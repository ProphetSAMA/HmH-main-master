package fun.wsss.hmh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws")
@Component
public class WebSocketServer {
    private static final Logger log = LoggerFactory.getLogger(WebSocketServer.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 用户会话 - 使用 ConcurrentHashMap 存储会话
     * key: 用户ID
     * value: 会话
     */
    private static final Map<Integer, WebSocketServer> USER_SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, WebSocketServer> MANAGER_SESSIONS = new ConcurrentHashMap<>();

    private Session session;
    private Integer userId;
    private String role;

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        log.info("新的WebSocket连接已建立，sessionId: {}", session.getId());
    }

    @OnClose
    public void onClose() {
        cleanupConnection();
        log.info("WebSocket连接已关闭，当前用户数: {}, 经理数: {}", USER_SESSIONS.size(), MANAGER_SESSIONS.size());
    }

    /**
     * 接收消息
     *
     * @param message 消息内容
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Map<String, Object> data = OBJECT_MAPPER.readValue(message, Map.class);
            String type = (String) data.get("type");

            if ("register".equals(type)) {
                // 清理旧连接
                cleanupConnection();

                this.role = (String) data.get("role");
                Integer newUserId = (Integer) data.get("userId");

                if (newUserId != null) {
                    // 如果该用户已有连接，先关闭旧连接
                    WebSocketServer existingSession = USER_SESSIONS.get(newUserId);
                    if (existingSession != null && existingSession != this) {
                        try {
                            existingSession.session.close();
                        } catch (IOException e) {
                            log.error("关闭旧连接失败", e);
                        }
                    }

                    this.userId = newUserId;
                    USER_SESSIONS.put(newUserId, this);
                    log.info("用户 {} 已注册，当前用户总数: {}", userId, USER_SESSIONS.size());
                }

                if ("经理".equals(this.role)) {
                    String sessionId = session.getId();
                    // 如果该会话ID已存在，先移除旧的
                    WebSocketServer existingManager = MANAGER_SESSIONS.get(sessionId);
                    if (existingManager != null && existingManager != this) {
                        MANAGER_SESSIONS.remove(sessionId);
                    }
                    MANAGER_SESSIONS.put(sessionId, this);
                    log.info("经理角色注册，当前经理数量：{}", MANAGER_SESSIONS.size());
                }
            }
        } catch (Exception e) {
            log.error("处理消息失败：{}", e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误：{}", error.getMessage());
        error.printStackTrace(); // 打印堆栈信息
        cleanupConnection();
    }

    private void cleanupConnection() {
        if (this.userId != null) {
            WebSocketServer storedSession = USER_SESSIONS.get(this.userId);
            if (storedSession == this) {
                USER_SESSIONS.remove(this.userId);
                log.info("已清理用户 {} 的连接", this.userId);
            }
        }

        if (this.session != null) {
            String sessionId = this.session.getId();
            WebSocketServer storedManager = MANAGER_SESSIONS.get(sessionId);
            if (storedManager == this) {
                MANAGER_SESSIONS.remove(sessionId);
                log.info("已清理经理的连接，sessionId: {}", sessionId);
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        if (this.session != null && this.session.isOpen()) {
            this.session.getBasicRemote().sendText(message);
        }
    }

    /**
     * 向指定用户发送消息
     *
     * @param userId  用户ID
     * @param message 消息内容
     */
    public static void notifyUser(Integer userId, String message) {
        WebSocketServer server = USER_SESSIONS.get(userId);
        if (server != null) {
            try {
                server.sendMessage(message);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败：{}", userId, e.getMessage());
            }
        }
    }

    /**
     * 向所有经理发送消息
     *
     * @param message 消息内容
     */
    public static void notifyManagers(String message) {
        MANAGER_SESSIONS.values().forEach(item -> {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                log.error("发送消息给经理失败：{}", e.getMessage());
            }
        });
    }

    /**
     * 向所有用户发送消息
     *
     * @param message 消息内容
     */
    public static void sendInfo(String message) {
        USER_SESSIONS.values().forEach(item -> {
            try {
                item.sendMessage(message);
            } catch (IOException e) {
                log.error("发送消息失败：{}", e.getMessage());
            }
        });
    }
} 