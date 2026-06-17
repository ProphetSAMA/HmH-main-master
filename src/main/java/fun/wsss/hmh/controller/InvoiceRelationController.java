package fun.wsss.hmh.controller;

import fun.wsss.hmh.common.Result;
import fun.wsss.hmh.entity.Invoice;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.service.InvoiceService;
import fun.wsss.hmh.service.ReimburseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 发票关联控制器
 */
@RestController
@RequestMapping("/api/invoice/relation")
@RequiredArgsConstructor
public class InvoiceRelationController {
    
    private final InvoiceService invoiceService;
    private final ReimburseService reimburseService;
    
    /**
     * 关联发票和报销单
     */
    @PostMapping
    public Result relateInvoice(@RequestBody Map<String, Object> params) {
        Integer invoiceId = (Integer) params.get("invoiceId");
        Integer reimburseId = (Integer) params.get("reimburseId");
        
        if (invoiceId == null || reimburseId == null) {
            return Result.error().message("参数错误");
        }
        
        try {
            boolean success = invoiceService.relateInvoice(invoiceId, reimburseId);
            return success ? Result.success() : Result.error().message("关联失败");
        } catch (Exception e) {
            return Result.error().message("关联失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取报销单关联的发票列表
     */
    @GetMapping("/reimburse/{reimburseId}")
    public Result getInvoicesByReimburseId(@PathVariable Integer reimburseId) {
        try {
            List<Invoice> invoices = invoiceService.getInvoicesByReimburseId(reimburseId);
            return Result.success(invoices);
        } catch (Exception e) {
            return Result.error().message("获取发票列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取可用于关联的报销单列表
     */
    @GetMapping("/available-reimburse")
    public Result getAvailableReimburse() {
        try {
            List<Reimburse> reimburses = reimburseService.getAvailableForInvoice();
            return Result.success(reimburses);
        } catch (Exception e) {
            return Result.error().message("获取可用报销单失败: " + e.getMessage());
        }
    }
} 