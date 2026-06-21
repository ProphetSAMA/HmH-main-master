package fun.wsss.hmh.controller;

import fun.wsss.hmh.utils.OcrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OCR配置控制器
 * 提供OCR服务状态和配置信息
 */
@RestController
@RequestMapping("/api/ocr")
public class OcrConfigController {

    @Autowired
    private OcrUtil ocrUtil;

    /**
     * 获取OCR配置信息
     */
    @GetMapping("/config")
    public Map<String, Object> getOcrConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("mode", ocrUtil.getOcrMode());
        config.put("currentProvider", ocrUtil.getCloudProvider());
        config.put("availableCloudServices", ocrUtil.getAvailableCloudServices());
        return config;
    }

    /**
     * 获取可用的云端服务列表
     */
    @GetMapping("/providers")
    public Set<String> getAvailableProviders() {
        return ocrUtil.getAvailableCloudServices();
    }
}