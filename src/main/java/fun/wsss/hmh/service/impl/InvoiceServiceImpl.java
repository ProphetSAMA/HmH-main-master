package fun.wsss.hmh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import fun.wsss.hmh.entity.Invoice;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.service.InvoiceService;
import fun.wsss.hmh.utils.OcrUtil;
import fun.wsss.hmh.utils.UserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import fun.wsss.hmh.dao.InvoiceDao;
import fun.wsss.hmh.utils.UserContext;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

/**
 * 发票服务实现类
 */
@Service
public class InvoiceServiceImpl extends ServiceImpl<InvoiceDao, Invoice> implements InvoiceService {

    @Autowired
    private BaseMapper<Reimburse> reimburseMapper;
    
    @Autowired
    private OcrUtil ocrUtil;
    
    @Value("${upload.path}")
    private String uploadPath;
    
    @Override
    public Page<Invoice> getInvoicePage(Integer current, Integer size, String keyword) {
        // 创建分页对象
        Page<Invoice> page = new Page<>(current, size);
        
        // 创建查询条件
        LambdaQueryWrapper<Invoice> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键字，添加查询条件
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(Invoice::getInvoiceNo, keyword)
                    .or()
                    .like(Invoice::getInvoiceCode, keyword);
        }
        
        // 获取当前用户ID
        Integer userId = UserUtil.getCurrentUserId();
        
        // 只查询当前用户的发票
        queryWrapper.eq(Invoice::getCreateUserId, userId);
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(Invoice::getCreateTime);
        
        try {
            // 执行分页查询
            return this.page(page, queryWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            // 发生异常时返回空分页
            return new Page<>();
        }
    }
    
    @Override
    public boolean saveInvoice(Invoice invoice) {
        try {
            // 获取当前用户ID
            Integer userId = UserUtil.getCurrentUserId();
            
            // 新增发票
            if (invoice.getId() == null) {
                invoice.setCreateUserId(userId);
                invoice.setCreateTime(new Date());
                invoice.setStatus(0); // 未使用
                invoice.setUpdateTime(new Date()); // 添加更新时间
            } else {
                // 更新发票
                Invoice existingInvoice = getById(invoice.getId());
                if (existingInvoice == null || !existingInvoice.getCreateUserId().equals(userId)) {
                    return false; // 不允许修改不存在或不属于当前用户的发票
                }
                invoice.setUpdateTime(new Date());
            }
            
            // 检查发票是否重复
            if (checkDuplicateInvoice(invoice)) {
                // 如果是重复发票，可以返回特定的错误或者处理方式
                return false;
            }
            
            return saveOrUpdate(invoice);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Map<String, Object> ocrInvoice(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 保存文件
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + suffix;
            String filePath = uploadPath + File.separator + filename;
            
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            file.transferTo(dest);
            
            // 调用OCR识别
            Map<String, Object> ocrResult = ocrUtil.recognizeInvoice(filePath);
            
            // 提取发票信息
            String invoiceNo = (String) ocrResult.get("invoiceNo");
            String invoiceCode = (String) ocrResult.get("invoiceCode");
            
            // 新增校验
            if (invoiceNo == null || invoiceCode == null) {
                throw new RuntimeException("未能识别出发票号码或发票代码，请上传清晰的发票图片！");
            }
            
            // 检查发票是否重复
            long count = this.count(new LambdaQueryWrapper<Invoice>()
                    .eq(Invoice::getInvoiceNo, invoiceNo)
                    .eq(Invoice::getInvoiceCode, invoiceCode));
            if (count > 0) {
                result.put("duplicate", true);
                result.putAll(ocrResult);
                return result;
            }
            
            // 创建发票对象
            Invoice invoice = new Invoice();
            invoice.setInvoiceNo(invoiceNo);
            invoice.setInvoiceCode(invoiceCode);
            invoice.setAmount((java.math.BigDecimal) ocrResult.get("amount"));
            invoice.setInvoiceDate((Date) ocrResult.get("invoiceDate"));
            invoice.setSeller((String) ocrResult.get("seller"));
            invoice.setImageUrl("/uploads/" + filename);
            invoice.setStatus(0); // 未使用
            invoice.setCreateUserId(UserUtil.getCurrentUserId());
            invoice.setCreateTime(new Date());
            invoice.setUpdateTime(new Date());
            
            // 保存发票
            this.save(invoice);
            
            result.put("duplicate", false);
            result.putAll(ocrResult);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("OCR识别失败");
        }
    }
    
    @Override
    @Transactional
    public boolean relateInvoice(Integer invoiceId, Integer reimburseId) {
        // 获取发票
        Invoice invoice = this.getById(invoiceId);
        if (invoice == null || invoice.getStatus() == 1) {
            return false;
        }
        
        // 获取报销单
        Reimburse reimburse = reimburseMapper.selectById(reimburseId);
        if (reimburse == null) {
            return false;
        }
        
        // 更新发票状态
        invoice.setStatus(1);
        invoice.setReimburseId(reimburseId);
        invoice.setUpdateTime(new Date());
        this.updateById(invoice);
        
        return true;
    }
    
    @Override
    public boolean deleteInvoice(Integer id) {
        // 获取当前用户ID
        Integer userId = UserContext.getCurrentUser().getId();
        
        // 验证发票是否存在且属于当前用户
        Invoice invoice = getById(id);
        if (invoice == null || !invoice.getCreateUserId().equals(userId)) {
            return false;
        }
        
        // 如果发票已关联报销单，不允许删除
        if (invoice.getStatus() == 1) {
            return false;
        }
        
        return removeById(id);
    }

    @Override
    public boolean checkDuplicateInvoice(Invoice invoice) {
        if (invoice == null || StringUtils.isBlank(invoice.getInvoiceNo()) || StringUtils.isBlank(invoice.getInvoiceCode())) {
            return false;
        }
        
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getInvoiceNo, invoice.getInvoiceNo())
               .eq(Invoice::getInvoiceCode, invoice.getInvoiceCode());
        
        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional
    public void associateInvoice(Reimburse reimburse, Invoice invoice) {
        // 设置发票信息
        invoice.setStatus(1); // 已关联
        invoice.setReimburseId(reimburse.getId());
        invoice.setCreateUserId(UserUtil.getCurrentUserId());
        invoice.setCreateTime(new Date());
        invoice.setUpdateTime(new Date());
        
        // 保存发票
        this.save(invoice);
    }

    @Override
    public List<Invoice> getInvoicesByReimburseId(Integer reimburseId) {
        if (reimburseId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<Invoice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Invoice::getReimburseId, reimburseId)
               .orderByDesc(Invoice::getCreateTime);
        
        return this.list(wrapper);
    }

    @Override
    public List<Invoice> getUserInvoices(Integer userId) {
        LambdaQueryWrapper<Invoice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Invoice::getCreateUserId, userId)
                .eq(Invoice::getStatus, 0) // 未使用的发票
                .orderByDesc(Invoice::getCreateTime);
        
        return list(queryWrapper);
    }
}