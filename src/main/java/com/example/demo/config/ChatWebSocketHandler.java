package com.example.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Handler xử lý kết nối real-time giữa Khách hàng và Shop trong phòng chat (US-59)
 */
@Slf4j
@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Quản lý danh sách các WebSocketSession theo maCuocTroChuyen
    private final Map<Long, Set<WebSocketSession>> phongChatSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long maCuocTroChuyen = trichXuatMaCuocTroChuyen(session.getUri());
        if (maCuocTroChuyen != null) {
            phongChatSessions.computeIfAbsent(maCuocTroChuyen, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(session);
            log.info("WebSocket kết nối thành công: sessionId={}, maCuocTroChuyen={}", session.getId(), maCuocTroChuyen);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("Nhận tin qua WebSocket: {}", message.getPayload());
        // Cho phép nhận tin nhắn ping/heartbeat hoặc payload từ client
        Long maCuocTroChuyen = trichXuatMaCuocTroChuyen(session.getUri());
        if (maCuocTroChuyen != null) {
            // Phản hồi heartbeat hoặc chuyển tiếp nếu cần
            session.sendMessage(new TextMessage("{\"trangThai\":\"DA_NHAN\"}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long maCuocTroChuyen = trichXuatMaCuocTroChuyen(session.getUri());
        if (maCuocTroChuyen != null && phongChatSessions.containsKey(maCuocTroChuyen)) {
            phongChatSessions.get(maCuocTroChuyen).remove(session);
            log.info("WebSocket ngắt kết nối: sessionId={}, maCuocTroChuyen={}", session.getId(), maCuocTroChuyen);
        }
    }

    /**
     * Phát sóng (broadcast) sự kiện tin nhắn mới / trả giá mới tới tất cả các client đang mở trong phòng chat này
     */
    public void broadcast(Long maCuocTroChuyen, Object payload) {
        Set<WebSocketSession> sessions = phongChatSessions.get(maCuocTroChuyen);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String json = objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Lỗi khi gửi bản tin WebSocket tới session {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi serialize payload WebSocket: ", e);
        }
    }

    private Long trichXuatMaCuocTroChuyen(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] idx = pair.split("=");
            if (idx.length == 2 && "maCuocTroChuyen".equalsIgnoreCase(idx[0])) {
                try {
                    return Long.parseLong(idx[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
