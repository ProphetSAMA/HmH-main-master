package fun.wsss.hmh.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import fun.wsss.hmh.common.Result;
import fun.wsss.hmh.entity.Invoice;
import fun.wsss.hmh.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 发票控制器
 */
@RestController
@RequestMapping("/api/invoice")
@RequiredArgsConstructor
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    /**
     * 获取发票分页列表
     */
    @GetMapping("/page")
    public Result getInvoicePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        try {
            Page<Invoice> page = invoiceService.getInvoicePage(current, size, keyword);
            return Result.success(page);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("获取发票列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存发票
     */
    @PostMapping("/save")
    public Result saveInvoice(@RequestBody Invoice invoice) {
        try {
            // 检查发票是否重复
            if (invoiceService.checkDuplicateInvoice(invoice)) {
                return Result.error().message("发票已存在，请勿重复添加");
            }
            
            boolean success = invoiceService.saveInvoice(invoice);
            return success ? Result.success() : Result.error().message("保存失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除发票
     */
    @DeleteMapping("/{id}")
    public Result deleteInvoice(@PathVariable Integer id) {
        boolean success = invoiceService.deleteInvoice(id);
        return success ? Result.success() : Result.error("删除失败");
    }
    
    /**
     * 获取用户可用发票列表
     */
    @GetMapping("/user")
    public Result getUserInvoices() {
        var user = fun.wsss.hmh.utils.UserContext.getCurrentUser();
        if (user == null) {
            return Result.error("用户未登录");
        }
        return Result.success(invoiceService.getUserInvoices(user.getId()));
    }
    
    /**
     * OCR识别发票
     */
    @PostMapping("/ocr")
    public Result ocr(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error().message("请选择文件");
        }
        
        try {
            Map<String, Object> result = invoiceService.ocrInvoice(file);
            return Result.ok().data(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error().message("OCR识别失败: " + e.getMessage());
        }
    }
    
    /**
     * 关联发票和报销单
     */
    @PostMapping("/relate")
    public Result relate(@RequestBody Map<String, Integer> params) {
        Integer invoiceId = params.get("invoiceId");
        Integer reimburseId = params.get("reimburseId");
        
        if (invoiceId == null || reimburseId == null) {
            return Result.error().message("参数错误");
        }
        
        boolean success = invoiceService.relateInvoice(invoiceId, reimburseId);
        if (success) {
            return Result.ok();
        } else {
            return Result.error().message("关联失败");
        }
    }
    
    /**
     * 获取发票详情
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Invoice invoice = invoiceService.getById(id);
        if (invoice != null) {
            return Result.ok().data("invoice", invoice);
        } else {
            return Result.error().message("发票不存在");
        }
    }
} 