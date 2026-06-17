package fun.wsss.hmh.service;

import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.entity.Reimburse;

/**
 * 通知服务接口
 * @author h
 */
public interface NotificationService {
    
    /**
     * 发送新报销申请通知给经理
     * @param reimburse 报销信息
     * @param applicant 申请人
     */
    void sendNewReimburseNotification(Reimburse reimburse, User applicant);
    
    /**
     * 发送报销审批通过通知
     * @param reimburse 报销信息
     * @param approver 审批人
     */
    void sendApprovalNotification(Reimburse reimburse, User approver);
    
    /**
     * 发送报销拒绝通知
     * @param reimburse 报销信息
     * @param approver 审批人
     */
    void sendRejectionNotification(Reimburse reimburse, User approver);
} 