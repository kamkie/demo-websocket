package net.devopssolutions.demo.ws.client.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.glassfish.tyrus.core.CloseReasons;
import org.glassfish.tyrus.ext.client.java8.SessionBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class WsConsumer {

    private final ClientManager clientManager = ClientManager.createClient(JdkClientContainer.class.getName());
    private final SessionBuilder sessionBuilder = new SessionBuilder(clientManager)
            .uri(new URI("ws://localhost:8080/ws"))
            .onOpen(this::onOpen)
            .messageHandler(PongMessage.class, new MessageHandler.Whole<PongMessage>() {//bug in jdk
                @Override
                public void onMessage(PongMessage pongMessage) {
                    WsConsumer.this.onPong(pongMessage);
                }
            })
            .messageHandler(ByteBuffer.class, this::onMessage)
            .messageHandler(String.class, this::onMessage)
            .onError(this::onError)
            .onClose(this::onClose);

    @Getter
    private final AtomicReference<Session> session = new AtomicReference<>();
    private final ByteBuffer ping = ByteBuffer.allocate(0);

    public WsConsumer() throws Exception {
        log.info("going to open ws connection");
        connect();
    }

    private boolean connect() {
        try {
            this.session.set(sessionBuilder.connect());
            return true;
        } catch (Exception e) {
            this.session.set(null);
            log.warn("couldn't connect", e);
            return false;
        }
    }

    private void onPong(PongMessage pongMessage) {
        log.info("incoming onPong: {}", pongMessage);
    }

    private void onMessage(ByteBuffer message) {
        log.info("new message: {}", message);
    }

    private void onMessage(String message) {
        log.info("new message: {}", message);
    }

    private void onOpen(Session session, EndpointConfig endpointConfig) {
        log.info("opened ws connection");
    }

    private void onClose(Session session, CloseReason closeReason) {
        log.info("closing ws connection");
    }

    private void onError(Session session, Throwable throwable) {
        log.warn("error in ws session: " + session, throwable);
    }

    @Scheduled(fixedRate = 10_000)
    private void sendPing() {
        Session session = this.session.get();
        log.info("sending ping: {}", session != null);
        if (session != null) {
            try {
                session.getBasicRemote().sendPing(ping);
            } catch (Exception e) {
                this.session.set(null);
                log.warn("exception sending ping", e);
            }
        } else {
            connect();
        }
    }

    @PreDestroy
    private void stop() {
        Session session = this.session.get();
        if (session != null && session.isOpen()) {
            try {
                session.close(CloseReasons.GOING_AWAY.getCloseReason());
            } catch (IOException e) {
                log.warn("exception closing session", e);
            }
        }
    }

}
