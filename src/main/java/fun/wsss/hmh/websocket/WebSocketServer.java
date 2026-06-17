package fun.wsss.hmh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务器
 */
@ServerEndpoint("/websocket/{userId}")
@Component
public class WebSocketServer {
    
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    
    /**
     * 用于存储在线用户的连接
     */
    private static final Map<Integer, Session> sessionMap = new ConcurrentHashMap<>();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Integer userId) {
        sessionMap.put(userId, session);
        log.info("用户{}连接成功，当前在线人数为{}", userId, sessionMap.size());
    }
    
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Integer userId) {
        sessionMap.remove(userId);
        log.info("用户{}断开连接，当前在线人数为{}", userId, sessionMap.size());
    }
    
    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, @PathParam("userId") Integer userId) {
        log.info("收到用户{}的消息：{}", userId, message);
    }
    
    /**
     * 发生错误时调用的方法
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误：{}", error.getMessage());
        error.printStackTrace();
    }
    
    /**
     * 发送消息给指定用户
     */
    public static void sendMessage(Integer userId, Object message) {
        Session session = sessionMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String jsonMessage = mapper.writeValueAsString(message);
                session.getBasicRemote().sendText(jsonMessage);
                log.info("发送消息给用户{}：{}", userId, jsonMessage);
            } catch (IOException e) {
                log.error("发送消息给用户{}失败：{}", userId, e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.warn("用户{}不在线", userId);
        }
    }
    
    /**
     * 发送消息给所有用户
     */
    public static void sendMessageToAll(Object message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(message);
            for (Map.Entry<Integer, Session> entry : sessionMap.entrySet()) {
                Session session = entry.getValue();
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(jsonMessage);
                }
            }
            log.info("发送消息给所有用户：{}", jsonMessage);
        } catch (IOException e) {
            log.error("发送消息给所有用户失败：{}", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 通知所有管理员
     * @param message 消息
     */
    public static void notifyManagers(String message) {
        // 实现通知所有管理员的逻辑
        sendMessageToAll(new NotificationMessage("manager", "系统通知", message));
    }

    /**
     * 发送消息
     * @param message 消息
     */
    public static void sendInfo(String message) {
        // 实现发送消息的逻辑
        sendMessageToAll(new NotificationMessage("info", "系统通知", message));
    }

    /**
     * 通知特定用户
     * @param userId 用户ID
     * @param message 消息
     */
    public static void notifyUser(Integer userId, String message) {
        // 实现通知特定用户的逻辑
        sendMessage(userId, new NotificationMessage("user", "系统通知", message));
    }
} 