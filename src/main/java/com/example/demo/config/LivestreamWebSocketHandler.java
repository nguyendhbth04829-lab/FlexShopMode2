package com.example.demo.config;

import com.example.demo.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
 * WebSocket Handler xử lý đồng bộ thời gian thực cho FlexShop Live (US-61):
 * - Ghim / gỡ ghim sản phẩm flash sale lập tức đến màn hình người xem
 * - Bình luận trực tiếp trong luồng live
 * - Thả tim tương tác (hiệu ứng bay tim)
 * - Đếm số lượng mắt xem thực tế (online viewers)
 * - Thông báo mua hàng nảy lên góc màn hình
 * - Chuyển tiếp tín hiệu WebRTC P2P (Live Camera stream giữa Host và Viewer)
 */
@Slf4j
@Component
public class LivestreamWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = createObjectMapper();

    // Quản lý danh sách các session theo maLive: Map<maLive, Set<WebSocketSession>>
    private final Map<Long, Set<WebSocketSession>> phongLiveSessions = new ConcurrentHashMap<>();

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Long maLive = trichXuatMaLive(session.getUri());
        if (maLive != null) {
            phongLiveSessions.computeIfAbsent(maLive, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(session);
            int viewerCount = getSoNguoiXemHienTai(maLive);
            log.info("WebSocket Live kết nối: sessionId={}, maLive={}, soNguoiXem={}", session.getId(), maLive, viewerCount);

            // Báo cho mọi người trong phòng biết số mắt xem hiện tại
            broadcast(maLive, ThongDiepLiveWebSocket.builder()
                    .loai("CAP_NHAT_NGUOI_XEM")
                    .maLive(maLive)
                    .duLieu(Map.of("soNguoiXem", viewerCount))
                    .build());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long maLive = trichXuatMaLive(session.getUri());
        if (maLive == null) return;

        try {
            Map<?, ?> data = objectMapper.readValue(message.getPayload(), Map.class);
            String loai = (String) data.get("loai");
            if (loai == null) return;

            if ("PING".equalsIgnoreCase(loai)) {
                synchronized (session) {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage("{\"loai\":\"PONG\"}"));
                    }
                }
            } else if ("THA_TIM".equalsIgnoreCase(loai)) {
                // Client bấm tim, broadcast hiệu ứng tim bay cho cả phòng
                broadcast(maLive, ThongDiepLiveWebSocket.builder()
                        .loai("THA_TIM")
                        .maLive(maLive)
                        .duLieu(data)
                        .build());
            } else if (loai.startsWith("WEBCAM_") || loai.startsWith("WEBRTC_")
                    || "CHECK_STREAM_STATUS".equalsIgnoreCase(loai)
                    || "REQUEST_WEBCAM_STREAM".equalsIgnoreCase(loai)
                    || "CHAT_SYNC".equalsIgnoreCase(loai)
                    || "BINH_LUAN_MOI".equalsIgnoreCase(loai)) {
                // Chuyển tiếp tín hiệu WebRTC P2P (Live Camera stream) & Chat realtime tới các client khác trong phòng
                broadcastExclude(maLive, session, message.getPayload());
            }
        } catch (Exception e) {
            log.warn("Lỗi phân tích payload WebSocket live: {}", e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long maLive = trichXuatMaLive(session.getUri());
        if (maLive != null && phongLiveSessions.containsKey(maLive)) {
            phongLiveSessions.get(maLive).remove(session);
            int viewerCount = getSoNguoiXemHienTai(maLive);
            log.info("WebSocket Live ngắt kết nối: sessionId={}, maLive={}, còn lại={}", session.getId(), maLive, viewerCount);

            // Cập nhật lại số mắt xem
            broadcast(maLive, ThongDiepLiveWebSocket.builder()
                    .loai("CAP_NHAT_NGUOI_XEM")
                    .maLive(maLive)
                    .duLieu(Map.of("soNguoiXem", viewerCount))
                    .build());
        }
    }

    /**
     * Phát sóng bản tin tới toàn bộ người dùng đang có mặt trong phòng live
     */
    public void broadcast(Long maLive, Object payload) {
        if (maLive == null) return;
        Set<WebSocketSession> sessions = phongLiveSessions.get(maLive);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String json = (payload instanceof String) ? (String) payload : objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session != null && session.isOpen()) {
                    synchronized (session) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.warn("Lỗi khi gửi bản tin WebSocket Live tới session {}: {}", session.getId(), e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi serialize payload WebSocket Live: ", e);
        }
    }

    /**
     * Phát sóng bản tin tới mọi client trong phòng trừ người gửi (cho tín hiệu P2P WebRTC / Sync)
     */
    public void broadcastExclude(Long maLive, WebSocketSession senderSession, Object payload) {
        if (maLive == null) return;
        Set<WebSocketSession> sessions = phongLiveSessions.get(maLive);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            String json = (payload instanceof String) ? (String) payload : objectMapper.writeValueAsString(payload);
            TextMessage textMessage = new TextMessage(json);

            for (WebSocketSession session : sessions) {
                if (session != null && session.isOpen() && (senderSession == null || !session.getId().equals(senderSession.getId()))) {
                    synchronized (session) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.warn("Lỗi gửi bản tin WebSocket Live tới session {}: {}", session.getId(), e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi broadcastExclude WebSocket Live: ", e);
        }
    }

    public int getSoNguoiXemHienTai(Long maLive) {
        if (maLive == null) return 0;
        Set<WebSocketSession> sessions = phongLiveSessions.get(maLive);
        return sessions != null ? sessions.size() : 0;
    }

    private Long trichXuatMaLive(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] idx = pair.split("=");
            if (idx.length == 2 && "maLive".equalsIgnoreCase(idx[0])) {
                try {
                    return Long.parseLong(idx[1]);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return null;
    }
}
