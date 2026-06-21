package fun.wsss.hmh.service.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 阿里云OCR服务实现
 * 使用java.net.http.HttpClient调用阿里云POP API
 */
@Slf4j
@Service
public class AliyunOcrService implements CloudOcrService {

    @Value("${ocr.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${ocr.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${ocr.aliyun.endpoint:ocr-api.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    private static final String ACTION = "RecognizeAdvanced";
    private static final String VERSION = "2021-07-07";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> recognizeInvoice(String filePath, String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 构建请求参数 (POP签名 v1)
            TreeMap<String, String> params = new TreeMap<>();
            params.put("Action", ACTION);
            params.put("Version", VERSION);
            params.put("Format", "JSON");
            params.put("AccessKeyId", accessKeyId);
            params.put("SignatureMethod", "HMAC-SHA1");
            params.put("Timestamp", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(new Date()));
            params.put("SignatureVersion", "1.0");
            params.put("SignatureNonce", UUID.randomUUID().toString());

            if (imageUrl != null && !imageUrl.isEmpty()) {
                params.put("Url", imageUrl);
            } else if (filePath != null && !filePath.isEmpty()) {
                byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());
                params.put("Body", Base64.getEncoder().encodeToString(fileBytes));
            } else {
                throw new IllegalArgumentException("必须提供文件路径或图片URL");
            }

            // 生成签名
            String signature = generatePopSignature(params);
            params.put("Signature", signature);

            // 构建POST表单
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formBody.length() > 0) formBody.append("&");
                formBody.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
            }

            // 发送请求
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://" + endpoint + "/"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(response.body());
            result.put("rawText", response.body());
            result.put("service", "aliyun");
            parseAliyunResult(rootNode, result);
            return result;
        } catch (Exception e) {
            log.error("阿里云OCR识别失败", e);
            throw new RuntimeException("阿里云OCR识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceName() {
        return "aliyun";
    }

    private String generatePopSignature(TreeMap<String, String> params) throws Exception {
        StringBuilder canonicalizedQueryString = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            canonicalizedQueryString.append("&").append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
        }
        String stringToSign = "POST&%2F&" + urlEncode(canonicalizedQueryString.substring(1));
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec((accessKeySecret + "&").getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
        byte[] signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signData);
    }

    private String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        } catch (Exception e) {
            return value;
        }
    }

    private void parseAliyunResult(JsonNode rootNode, Map<String, Object> result) {
        try {
            JsonNode data = rootNode.has("Data") ? rootNode.get("Data") : rootNode;
            JsonNode dataNode = data.isTextual() ? objectMapper.readTree(data.asText()) : data;
            if (dataNode.has("InvoiceNo")) result.put("invoiceNo", dataNode.get("InvoiceNo").asText());
            if (dataNode.has("InvoiceCode")) result.put("invoiceCode", dataNode.get("InvoiceCode").asText());
            if (dataNode.has("TotalAmount")) {
                String amountStr = dataNode.get("TotalAmount").asText();
                if (amountStr != null && !amountStr.isEmpty()) result.put("amount", new java.math.BigDecimal(amountStr));
            }
            if (dataNode.has("SellerName")) result.put("seller", dataNode.get("SellerName").asText());
            if (dataNode.has("InvoiceDate")) result.put("invoiceDateStr", dataNode.get("InvoiceDate").asText());
        } catch (Exception e) {
            log.warn("解析阿里云OCR结果失败", e);
        }
    }
}