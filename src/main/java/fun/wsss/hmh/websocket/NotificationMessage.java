package fun.wsss.hmh.websocket;

import lombok.Data;

import java.util.Date;

/**
 * 通知消息
 */
@Data
public class NotificationMessage {
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息时间
     */
    private Date time;
    
    /**
     * 相关ID
     */
    private Integer relatedId;
    
    /**
     * 构造函数
     */
    public NotificationMessage() {
        this.time = new Date();
    }
    
    /**
     * 构造函数
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     */
    public NotificationMessage(String type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.time = new Date();
    }
    
    /**
     * 构造函数
     * @param type 消息类型
     * @param title 消息标题
     * @param content 消息内容
     * @param relatedId 相关ID
     */
    public NotificationMessage(String type, String title, String content, Integer relatedId) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.relatedId = relatedId;
        this.time = new Date();
    }
} 