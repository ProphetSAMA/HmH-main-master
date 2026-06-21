package fun.wsss.hmh.utils;

import fun.wsss.hmh.service.ocr.CloudOcrService;
import fun.wsss.hmh.service.ocr.LocalOcrService;
import fun.wsss.hmh.service.ocr.OcrServiceFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * OCR识别工具类
 * 支持本地OCR和云端OCR服务
 */
@Slf4j
@Component
public class OcrUtil {

    @Autowired
    private OcrServiceFactory ocrServiceFactory;

    @Autowired
    private LocalOcrService localOcrService;

    /**
     * 识别发票
     * @param filePath 文件路径
     * @return 识别结果
     */
    public Map<String, Object> recognizeInvoice(String filePath) {
        String mode = ocrServiceFactory.getOcrMode();
        log.info("OCR模式: {}", mode);

        if ("cloud".equalsIgnoreCase(mode)) {
            return recognizeWithCloud(filePath, null);
        } else {
            return recognizeWithLocal(filePath);
        }
    }

    /**
     * 识别发票（支持URL）
     * @param filePath 文件路径
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    public Map<String, Object> recognizeInvoice(String filePath, String imageUrl) {
        String mode = ocrServiceFactory.getOcrMode();
        log.info("OCR模式: {}", mode);

        if ("cloud".equalsIgnoreCase(mode)) {
            return recognizeWithCloud(filePath, imageUrl);
        } else {
            return recognizeWithLocal(filePath);
        }
    }

    /**
     * 使用本地OCR识别
     * @param filePath 文件路径
     * @return 识别结果
     */
    private Map<String, Object> recognizeWithLocal(String filePath) {
        log.info("使用本地Tesseract OCR识别");
        return localOcrService.recognizeInvoice(filePath);
    }

    /**
     * 使用云端OCR识别
     * @param filePath 文件路径
     * @param imageUrl 图片URL
     * @return 识别结果
     */
    private Map<String, Object> recognizeWithCloud(String filePath, String imageUrl) {
        CloudOcrService cloudService = ocrServiceFactory.getCloudOcrService();
        if (cloudService == null) {
            log.warn("云端OCR服务未配置，回退到本地OCR");
            return recognizeWithLocal(filePath);
        }

        log.info("使用云端OCR服务: {}", cloudService.getServiceName());
        return cloudService.recognizeInvoice(filePath, imageUrl);
    }

    /**
     * 获取当前OCR模式
     * @return OCR模式
     */
    public String getOcrMode() {
        return ocrServiceFactory.getOcrMode();
    }

    /**
     * 获取当前云端服务商
     * @return 云端服务商名称
     */
    public String getCloudProvider() {
        if ("cloud".equalsIgnoreCase(ocrServiceFactory.getOcrMode())) {
            CloudOcrService service = ocrServiceFactory.getCloudOcrService();
            return service != null ? service.getServiceName() : "none";
        }
        return "local";
    }

    /**
     * 获取所有可用的云端服务
     * @return 可用服务列表
     */
    public java.util.Set<String> getAvailableCloudServices() {
        return ocrServiceFactory.getAvailableCloudServices();
    }
}