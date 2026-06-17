package fun.wsss.hmh.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fun.wsss.hmh.entity.Reimburse;
import fun.wsss.hmh.entity.User;
import fun.wsss.hmh.service.NotificationService;
import fun.wsss.hmh.websocket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知服务实现类
 * @author h
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送新报销申请通知
     * @param reimburse 报销信息
     * @param applicant 申请人
     */
    @Override
    public void sendNewReimburseNotification(Reimburse reimburse, User applicant) {
        try {
            // 发送通知
            Map<String, Object> notification = new HashMap<>(4);
            // 通知类型
            notification.put("type", "notification");
            // 通知标题
            notification.put("title", "新的报销申请");
            // 通知内容
            notification.put("message", String.format("%s 提交了一笔新的报销申请，金额：%.2f元", 
                applicant.getTrueName(), reimburse.getMoney()));
            notification.put("notificationType", "info");

            WebSocketServer.notifyManagers(objectMapper.writeValueAsString(notification));
        } catch (Exception e) {
            log.error("发送新报销申请通知失败", e);
        }
    }

    /**
     * 发送审批通过通知
     * @param reimburse 报销信息
     * @param approver 审批人
     */
    @Override
    public void sendApprovalNotification(Reimburse reimburse, User approver) {
        try {
            Map<String, Object> notification = new HashMap<>(4);
            notification.put("type", "notification");
            notification.put("title", "报销申请已通过");
            notification.put("message", String.format("报销申请已通过审批！审批人：%s", approver.getTrueName()));
            notification.put("notificationType", "success");
            
            WebSocketServer.sendInfo(objectMapper.writeValueAsString(notification));
        } catch (Exception e) {
            log.error("发送审批通过通知失败", e);
        }
    }

    /**
     * 发送拒绝通知
     * @param reimburse 报销信息
     * @param approver 审批人
     */
    @Override
    public void sendRejectionNotification(Reimburse reimburse, User approver) {
        try {
            Map<String, Object> notification = new HashMap<>(4);
            notification.put("type", "notification");
            notification.put("title", "报销申请被拒绝");
            notification.put("message", String.format("报销申请被拒绝。原因：%s，审批人：%s", 
                reimburse.getDenyReason(), approver.getTrueName()));
            notification.put("notificationType", "warning");
            
            WebSocketServer.notifyUser(reimburse.getSqUserId(), objectMapper.writeValueAsString(notification));
        } catch (Exception e) {
            log.error("发送拒绝通知失败", e);
        }
    }
} 