package fun.wsss.hmh.service.ocr;

import java.util.Map;

/**
 * 云端OCR服务接口
 */
public interface CloudOcrService {

    /**
     * 识别发票
     * @param filePath 文件路径
     * @param imageUrl 图片URL（可选，某些云服务支持URL识别）
     * @return 识别结果
     */
    Map<String, Object> recognizeInvoice(String filePath, String imageUrl);

    /**
     * 获取服务名称
     * @return 服务名称
     */
    String getServiceName();
}