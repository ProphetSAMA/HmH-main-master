package fun.wsss.hmh.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.Invoice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 发票Dao接口
 */
@Mapper
public interface InvoiceDao extends BaseMapper<Invoice> {
    // 根据发票号码查询发票
    Invoice getInvoiceByNumber(String invoiceNumber);

    // 根据报销单ID获取关联的发票列表
    List<Invoice> getInvoicesByReimburseId(Integer reimburseId);
}