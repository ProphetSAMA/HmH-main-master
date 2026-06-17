package fun.wsss.hmh.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import fun.wsss.hmh.entity.Invoice;
import fun.wsss.hmh.entity.Reimburse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 发票服务接口
 */
public interface InvoiceService extends IService<Invoice> {
    
    /**
     * 获取发票分页列表
     * @param current 当前页
     * @param size 每页大小
     * @param keyword 关键字
     * @return 分页数据
     */
    Page<Invoice> getInvoicePage(Integer current, Integer size, String keyword);
    
    /**
     * 保存发票
     * @param invoice 发票信息
     * @return 是否成功
     */
    boolean saveInvoice(Invoice invoice);
    
    /**
     * 删除发票
     * @param id 发票ID
     * @return 是否成功
     */
    boolean deleteInvoice(Integer id);
    
    /**
     * 获取用户的发票列表
     * @param userId 用户ID
     * @return 发票列表
     */
    List<Invoice> getUserInvoices(Integer userId);
    
    Map<String, Object> ocrInvoice(MultipartFile file);
    
    boolean relateInvoice(Integer invoiceId, Integer reimburseId);
    
    boolean checkDuplicateInvoice(Invoice invoice);
    
    void associateInvoice(Reimburse reimburse, Invoice invoice);
    
    List<Invoice> getInvoicesByReimburseId(Integer reimburseId);
}