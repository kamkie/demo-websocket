package net.devopssolutions.demo.ws.server.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WsHandler implements WebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("afterConnectionEstablished session: {}", session);
        sessions.put(session.getId(), session);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("handleMessage session: {}, message: {}", session, message);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        boolean remove = sessions.remove(session.getId(), session);
        log.info("handleTransportError session: " + session + ", remove: " + remove, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        boolean remove = sessions.remove(session.getId(), session);
        log.info("afterConnectionClosed session: {}, closeStatus: {}, remove: {}", session, closeStatus, remove);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Scheduled(fixedRate = 3_000)
    private void sendHello() {
        log.info("sending hello: {}", sessions.size());
        sessions.forEach(10, this::sendHello);
    }

    private void sendHello(String id, WebSocketSession session) {
        log.info("sending hello sessionId: {}, session {}", id, session);
        try {
            session.sendMessage(new TextMessage("Hello World"));
        } catch (Exception e) {
            boolean remove = sessions.remove(id, session);
            log.warn("exception sending hello, remove session " + remove, e);
        }
    }

    @Scheduled(fixedRate = 10_000)
    private void sendPings() {
        log.info("sending pings: {}", sessions.size());
        sessions.forEach(10, this::sendPing);
    }

    private void sendPing(String id, WebSocketSession session) {
        log.info("sending ping sessionId: {}, session {}", id, session);
        try {
            session.sendMessage(new PingMessage());
        } catch (Exception e) {
            boolean remove = sessions.remove(id, session);
            log.warn("exception sending ping, remove session " + remove, e);
        }
    }

}
