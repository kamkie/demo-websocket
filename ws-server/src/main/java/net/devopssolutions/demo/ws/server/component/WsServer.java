package net.devopssolutions.demo.ws.server.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

@Slf4j
@Component
public class WsServer extends Endpoint {

    private final ByteBuffer ping = ByteBuffer.allocate(0);
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
    private final LoggingThreadPoolExecutor handlersExecutor = new LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10_000));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RpcMethodDispatcher rpcMethodDispatcher;

    public Set<Session> getAllSessions() {
        return this.sessions.values()
                .stream()
                .map(Session::getOpenSessions)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    public void onBinaryMessage(InputStream message, Session session) {
        log.info("onBinaryMessage id: {}, message: {}", session.getId(), message);
        Principal userPrincipal = session.getUserPrincipal();
        try (GZIPInputStream inputStream = new GZIPInputStream(message)) {
            JsonNode node = objectMapper.readTree(inputStream);
            handlersExecutor.execute(() -> {
                dispatchMessage(node, userPrincipal);
            });
        } catch (Exception e) {
            log.warn("exception handling ws message session: " + session.getId(), e);
        }
    }

    public void onTextMessage(String message, Session session) {
        log.info("onTextMessage id: {}, message: {}", session.getId(), message);
        handlersExecutor.execute(() -> {
            try {
                JsonNode node = objectMapper.readTree(message);
                dispatchMessage(node, session.getUserPrincipal());
            } catch (Exception e) {
                log.warn("exception handling ws message session: " + session.getId(), e);
            }
        });
    }

    private void dispatchMessage(JsonNode node, Principal principal) {
        String id = node.get("id").asText();
        String method = node.get("method").asText();
        JsonNode params = node.get("params");
        rpcMethodDispatcher.handle(id, method, params, principal);
    }

    public void onPong(PongMessage message, Session session) {
        log.info("onPong id: {}, message: {}", session.getId(), message);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        log.info("onOpen id: {}", session.getId());

        session.setMaxIdleTimeout(10_000);
        session.setMaxBinaryMessageBufferSize(100_000_000);
        session.setMaxTextMessageBufferSize(1_000_000);
        session.addMessageHandler(PongMessage.class, message -> onPong(message, session));
        session.addMessageHandler(String.class, message -> onTextMessage(message, session));
        session.addMessageHandler(InputStream.class, message -> onBinaryMessage(message, session));

        sessions.put(session.getId(), session);
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        log.info("onClose id: {} closeReason: {}", session.getId(), closeReason);
        sessions.remove(session.getId(), session);
    }

    @Override
    public void onError(Session session, Throwable exception) {
        log.info("onError id: " + session.getId(), exception);
        sessions.remove(session.getId(), session);
        try {
            session.close(new CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, exception.getMessage()));
        } catch (IOException e) {
            log.warn("exception closing session id: " + session.getId(), e);
        }
    }

    @Scheduled(fixedRate = 10_000)
    public void sendPings() {
        log.info("sending pings: {}", sessions.size());
        getAllSessions()
                .forEach(this::sendPing);
    }

    private void sendPing(Session session) {
        log.info("sending ping sessionId: {}, session {}", session.getId(), session);
        try {
            session.getBasicRemote().sendPing(ping);
        } catch (Exception e) {
            boolean remove = sessions.remove(session.getId(), session);
            log.warn("exception sending ping, remove session " + remove, e);
        }
    }

}