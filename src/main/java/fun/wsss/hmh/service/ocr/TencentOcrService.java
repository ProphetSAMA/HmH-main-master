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
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 腾讯云OCR服务实现
 * 使用java.net.http.HttpClient调用腾讯云REST API
 */
@Slf4j
@Service
public class TencentOcrService implements CloudOcrService {

    @Value("${ocr.tencent.secret-id:}")
    private String secretId;

    @Value("${ocr.tencent.secret-key:}")
    private String secretKey;

    @Value("${ocr.tencent.region:ap-guangzhou}")
    private String region;

    private static final String SERVICE = "ocr";
    private static final String HOST = "ocr.tencentcloudapi.com";
    private static final String ENDPOINT = "https://" + HOST;
    private static final String VERSION = "2018-11-19";
    private static final String ACTION = "VatInvoiceOCR";
    private static final String ALGORITHM = "TC3-HMAC-SHA256";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> recognizeInvoice(String filePath, String imageUrl) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 构建请求体
            Map<String, Object> bodyMap = new HashMap<>();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                bodyMap.put("ImageUrl", imageUrl);
            } else if (filePath != null && !filePath.isEmpty()) {
                byte[] fileBytes = Files.readAllBytes(new File(filePath).toPath());
                bodyMap.put("ImageBase64", Base64.getEncoder().encodeToString(fileBytes));
            } else {
                throw new IllegalArgumentException("必须提供文件路径或图片URL");
            }
            String payload = objectMapper.writeValueAsString(bodyMap);

            // 时间戳
            long currentTimeMillis = System.currentTimeMillis();
            long timestamp = currentTimeMillis / 1000;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateStr = sdf.format(new Date(currentTimeMillis));

            // 规范请求
            String contentType = "application/json; charset=utf-8";
            String canonicalHeaders = "content-type:" + contentType + "\n" + "host:" + HOST + "\n";
            String signedHeaders = "content-type;host";
            String hashedPayload = sha256Hex(payload);
            String canonicalRequest = "POST\n/\n\n" + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedPayload;

            // 待签名字符串
            String credentialScope = dateStr + "/" + SERVICE + "/" + "tc3_request";
            String stringToSign = ALGORITHM + "\n" + timestamp + "\n" + credentialScope + "\n" + sha256Hex(canonicalRequest);

            // 计算签名
            byte[] secretDate = hmacSha256(("TC3" + secretKey).getBytes(StandardCharsets.UTF_8), dateStr);
            byte[] secretService = hmacSha256(secretDate, SERVICE);
            byte[] secretSigning = hmacSha256(secretService, "tc3_request");
            String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

            String authorization = ALGORITHM + " Credential=" + secretId + "/" + credentialScope
                    + ", SignedHeaders=" + signedHeaders + ", Signature=" + signature;

            // 发送请求
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .header("Content-Type", contentType)
                    .header("Host", HOST)
                    .header("X-TC-Action", ACTION)
                    .header("X-TC-Version", VERSION)
                    .header("X-TC-Timestamp", String.valueOf(timestamp))
                    .header("X-TC-Region", region)
                    .header("Authorization", authorization)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode rootNode = objectMapper.readTree(response.body());
            result.put("rawText", response.body());
            result.put("service", "tencent");
            if (rootNode.has("Response")) {
                parseTencentResult(rootNode.get("Response"), result);
            }
            return result;
        } catch (Exception e) {
            log.error("腾讯云OCR识别失败", e);
            throw new RuntimeException("腾讯云OCR识别失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getServiceName() {
        return "tencent";
    }

    private void parseTencentResult(JsonNode responseNode, Map<String, Object> result) {
        try {
            if (!responseNode.has("VatInvoiceInfos")) return;
            for (JsonNode item : responseNode.get("VatInvoiceInfos")) {
                String name = item.has("Name") ? item.get("Name").asText() : null;
                String value = item.has("Value") ? item.get("Value").asText() : null;
                if (name == null || value == null) continue;
                switch (name) {
                    case "发票号码": result.put("invoiceNo", value); break;
                    case "发票代码": result.put("invoiceCode", value); break;
                    case "开票日期": result.put("invoiceDateStr", value); break;
                    case "合计金额": result.put("amount", new java.math.BigDecimal(value.replace("¥", "").replace(",", "").trim())); break;
                    case "销售方名称": result.put("seller", value); break;
                    case "校验码": result.put("checkCode", value); break;
                    case "价税合计": result.put("totalTaxAmount", value); break;
                }
            }
        } catch (Exception e) {
            log.warn("解析腾讯云OCR结果失败", e);
        }
    }

    private String sha256Hex(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return bytesToHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
    }

    private byte[] hmacSha256(byte[] key, String msg) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(msg.getBytes(StandardCharsets.UTF_8));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}