package net.devopssolutions.demo.ws.server.component;

import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class WsBroadcaster {

    private final LoggingThreadPoolExecutor sendersExecutor = new LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10_000));

    @Autowired
    private WsServer wsServer;

    public void broadcastAsync(Consumer<Session> action) {
        wsServer.getAllSessions().forEach(session -> {
            sendersExecutor.execute(() -> {
                try {
                    action.accept(session);
                } catch (Throwable throwable) {
                    log.warn("exception broadcasting message to session: " + session, throwable);
                }
            });
        });
    }

    public void sendToId(String sessionId, Consumer<Session> action) {
        Session session = wsServer.getSession(sessionId);
        if (session == null) {
            log.warn("session with id {} not founded", sessionId);
            return;
        }
        try {
            action.accept(session);
        } catch (Throwable throwable) {
            log.warn("exception sending message to session: " + session, throwable);
        }
    }

    public void sendToIdAsync(String sessionId, Consumer<Session> action) {
        sendersExecutor.execute(() -> sendToId(sessionId, action));
    }



}
