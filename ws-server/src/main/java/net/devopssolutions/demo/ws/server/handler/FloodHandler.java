package net.devopssolutions.demo.ws.server.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.rpc.*;
import net.devopssolutions.demo.ws.server.component.WsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
@RpcMethod(RpcMethods.FLOOD)
public class FloodHandler implements RpcMethodHandler {

    @Autowired
    private WsServer wsServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(String id, Object params, Principal user) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params);

        Set<Session> sessions = wsServer.getAllSessions().stream()
                .collect(Collectors.toSet());

        IntStream.range(0, ((IntNode) params).intValue())
                .parallel()
                .mapToObj(operand -> createRpcMessage(id, operand))
                .map(this::getByteBuffer)
                .filter(byteBuffer -> byteBuffer != null)
                .forEach(byteBuffer -> sessions.forEach(session -> {
                    synchronized (session) {
                        try {
                            session.getBasicRemote().sendBinary(byteBuffer);
                        } catch (IOException e) {
                            log.warn("exception sending data: session: " + session, e);
                        }
                    }
                }));
    }

    private ByteBuffer getByteBuffer(RpcMessage<Object, Object> value) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectMapper.writeValue(new GZIPOutputStream(byteArrayOutputStream), value);
            return ByteBuffer.wrap(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            log.warn("exception sending message");
        }
        return null;
    }

    private RpcMessage<Object, Object> createRpcMessage(String id, Object response) {
        return RpcMessage.builder()
                .id(id)
                .method(RpcMethods.FLOOD)
                .type(RpcType.RESPONSE)
                .created(LocalDateTime.now(ZoneOffset.UTC))
                .response(response)
                .build();
    }

}
