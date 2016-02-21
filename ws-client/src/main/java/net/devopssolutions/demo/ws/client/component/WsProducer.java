package net.devopssolutions.demo.ws.client.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.rpc.RpcMessage;
import net.devopssolutions.demo.ws.rpc.RpcMethods;
import net.devopssolutions.demo.ws.rpc.RpcType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
public class WsProducer {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WsConsumer wsConsumer;

    @Scheduled(fixedRate = 10_000)
    private void sendHello() {
        Session session = wsConsumer.getSession().get();
        log.info("sending hello: {}", session != null);
        if (session != null) {
            try {
                RpcMessage<Object, Object> message = RpcMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .method(RpcMethods.HELLO)
                        .type(RpcType.REQUEST)
                        .params("word")
                        .build();
                sendMessage(session, message);
            } catch (Exception e) {
                log.warn("exception sending hello", e);
            }
        }
    }

    @Scheduled(fixedRate = 30_000, initialDelay = 10_000)
    private void sendFlood() {
        Session session = wsConsumer.getSession().get();
        log.info("sending hello: {}", session != null);
        if (session != null) {
            try {
                RpcMessage<Object, Object> message = RpcMessage.builder()
                        .id(UUID.randomUUID().toString())
                        .method(RpcMethods.FLOOD)
                        .type(RpcType.REQUEST)
                        .params(1_000_000)
                        .build();
                sendMessage(session, message);
            } catch (Exception e) {
                log.warn("exception sending hello", e);
            }
        }
    }

    private void sendMessage(Session session, RpcMessage<Object, Object> message) throws IOException {
        OutputStream sendStream = null;
        try {
            sendStream = session.getBasicRemote().getSendStream();
            objectMapper.writeValue(new GZIPOutputStream(sendStream), message);
        } catch (IOException e) {
            if (sendStream != null) {
                sendStream.close();
            }
            throw e;
        }
    }

}
