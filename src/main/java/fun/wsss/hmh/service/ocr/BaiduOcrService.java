package fun.wsss.hmh.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * 百度云OCR服务实现
 * 使用java.net.http.HttpClient调用百度云REST API
 */
@Slf4j
@Service
public class BaiduOcrService implements CloudOcrService {

    @Value("${ocr.baidu.api-key:}")
    private String apiKey;

    @Value("${ocr.baidu.secret-key:}")
    private String secretKey;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";
    private static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/vat_invoice";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> recognizeInvoice(String filePath, String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        try {
            String accessToken = getAccessToken();

            // 构建请求体
            StringBuilder formBody = new StringBuilder();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                formBody.append("url=").append(URLEncoder.encode(imageUrl, StandardCharsets.UTF_8));
            } else if (filePath != null && !filePath.isEmpty()) {
                byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());
                formBody.append("image=").append(URLEncoder.encode(Base64.getEncoder().encodeToString(fileBytes), StandardCharsets.UTF_8));
            } else {
                throw new IllegalArgumentException("必须提供文件路径或图片URL");
            }

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OCR_URL + "?access_token=" + accessToken))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(response.body());
            result.put("rawText", response.body());
            result.put("service", "baidu");
            parseBaiduResult(rootNode, result);
            return result;
        } catch (Exception e) {
            log.error("百度云OCR识别失败", e);
            throw new RuntimeException("百度云OCR识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceName() {
        return "baidu";
    }

    private String getAccessToken() throws Exception {
        String formBody = "grant_type=client_credentials&client_id=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode rootNode = objectMapper.readTree(response.body());
        if (rootNode.has("access_token")) {
            return rootNode.get("access_token").asText();
        }
        throw new RuntimeException("获取百度云access_token失败: " + response.body());
    }

    private void parseBaiduResult(JsonNode rootNode, Map<String, Object> result) {
        try {
            if (!rootNode.has("words_result")) return;
            JsonNode wordsResult = rootNode.get("words_result");
            if (wordsResult.has("InvoiceNum")) result.put("invoiceNo", wordsResult.get("InvoiceNum").asText());
            if (wordsResult.has("InvoiceCode")) result.put("invoiceCode", wordsResult.get("InvoiceCode").asText());
            if (wordsResult.has("TotalAmount")) {
                String amountStr = wordsResult.get("TotalAmount").asText().replace("¥", "").replace(",", "").trim();
                if (!amountStr.isEmpty()) result.put("amount", new java.math.BigDecimal(amountStr));
            }
            if (wordsResult.has("SellerName")) result.put("seller", wordsResult.get("SellerName").asText());
            if (wordsResult.has("InvoiceDate")) result.put("invoiceDateStr", wordsResult.get("InvoiceDate").asText());
            if (wordsResult.has("CheckCode")) result.put("checkCode", wordsResult.get("CheckCode").asText());
        } catch (Exception e) {
            log.warn("解析百度云OCR结果失败", e);
        }
    }
}