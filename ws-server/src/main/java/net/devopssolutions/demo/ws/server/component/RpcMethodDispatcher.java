package net.devopssolutions.demo.ws.server.component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.rpc.RpcMethod;
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RpcMethodDispatcher {

    @Getter
    private Map<String, RpcMethodHandler> handlerMap;

    @Autowired
    private void init(List<RpcMethodHandler> handlers) {
        Function<RpcMethodHandler, String> keyMapper = rpcMethodHandler -> rpcMethodHandler.getClass().getAnnotation(RpcMethod.class).value();
        handlerMap = handlers.stream().collect(Collectors.toMap(keyMapper, rpcMethodHandler -> rpcMethodHandler));
    }

    @SuppressWarnings("unchecked")
    public void handle(String id, String method, Object params, Principal user) {
        RpcMethodHandler handler = handlerMap.get(method);
        if (handler == null) {
            log.warn("no handler for method: {} ", method);
            return;
        }

        handler.handle(id, params, user);
    }
}
