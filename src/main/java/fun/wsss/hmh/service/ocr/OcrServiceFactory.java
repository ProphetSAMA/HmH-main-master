package fun.wsss.hmh.service.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OCR服务工厂类
 * 根据配置选择使用本地OCR或云端OCR服务
 */
@Slf4j
@Component
public class OcrServiceFactory {

    @Value("${ocr.mode:local}")
    private String ocrMode;

    @Value("${ocr.cloud-provider:aliyun}")
    private String cloudProvider;

    @Autowired(required = false)
    private List<CloudOcrService> cloudOcrServices;

    // 云端服务映射
    private Map<String, CloudOcrService> serviceMap;

    /**
     * 获取OCR服务模式
     * @return "local" 或 "cloud"
     */
    public String getOcrMode() {
        return ocrMode;
    }

    /**
     * 获取云端OCR服务
     * @return 云端OCR服务实例
     */
    public CloudOcrService getCloudOcrService() {
        if (serviceMap == null) {
            initServiceMap();
        }

        CloudOcrService service = serviceMap.get(cloudProvider.toLowerCase());
        if (service == null) {
            log.warn("未找到云服务商 {} 的实现，使用默认阿里云", cloudProvider);
            service = serviceMap.get("aliyun");
        }

        return service;
    }

    /**
     * 获取所有可用的云端服务名称
     * @return 服务名称列表
     */
    public java.util.Set<String> getAvailableCloudServices() {
        if (serviceMap == null) {
            initServiceMap();
        }
        return serviceMap.keySet();
    }

    /**
     * 初始化服务映射
     */
    private void initServiceMap() {
        serviceMap = new HashMap<>();
        if (cloudOcrServices != null) {
            for (CloudOcrService service : cloudOcrServices) {
                serviceMap.put(service.getServiceName().toLowerCase(), service);
                log.info("注册云端OCR服务: {}", service.getServiceName());
            }
        }
    }
}