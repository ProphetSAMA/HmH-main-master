package fun.wsss.hmh.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;
import net.sourceforge.tess4j.Tesseract;

/**
 * OCR识别工具类
 * 注意：这里只是一个示例，实际应用中需要集成第三方OCR服务
 * 如百度OCR、阿里云OCR等
 */
@Component
public class OcrUtil {
    
    /**
     * 识别发票
     * @param filePath 文件路径
     * @return 识别结果
     */
    public Map<String, Object> recognizeInvoice(String filePath) {
        Map<String, Object> result = new HashMap<>();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata"); // 路径请根据实际情况修改
        tesseract.setLanguage("chi_sim");

        try {
            String text = tesseract.doOCR(new File(filePath));
            result.put("rawText", text);
            System.out.println(text);

            // 发票号码
            Matcher mNo = Pattern.compile("发票号码[:：]?\\s*([0-9]{10,})").matcher(text);
            if (mNo.find()) result.put("invoiceNo", mNo.group(1));

            // 开票日期
            Matcher mDate = Pattern.compile("开票日期[:：]?\\s*([0-9]{4}[年/-][0-9]{1,2}[月/-][0-9]{1,2}日?)").matcher(text);
            if (mDate.find()) {
                String dateStr = mDate.group(1).replace("年", "-").replace("月", "-").replace("日", "");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                result.put("invoiceDate", sdf.parse(dateStr));
            }

            // 金额（优先价税合计，其次合计）
            Matcher mTotal = Pattern.compile("价税合计.*?（小写）?¥?([0-9]+\\.?[0-9]*)").matcher(text);
            if (mTotal.find()) {
                result.put("amount", new BigDecimal(mTotal.group(1)));
            } else {
                Matcher mHeji = Pattern.compile("合计\\s*¥?([0-9]+\\.?[0-9]*)").matcher(text);
                if (mHeji.find()) result.put("amount", new BigDecimal(mHeji.group(1)));
            }

            // 销售方
            Matcher mSeller = Pattern.compile("名称[:：]?([\\u4e00-\\u9fa5A-Za-z0-9（）()·\\s]+)").matcher(text);
            String seller = null;
            while (mSeller.find()) {
                String name = mSeller.group(1).trim();
                if (name.length() > 4) {
                    seller = name;
                    break;
                }
            }
            if (seller != null) result.put("seller", seller);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("OCR识别失败", e);
        }
    }
} 