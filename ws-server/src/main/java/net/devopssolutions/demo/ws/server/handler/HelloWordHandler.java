package net.devopssolutions.demo.ws.server.handler;

import lombok.extern.slf4j.Slf4j;
import net.devopssolutions.demo.ws.rpc.RpcMethod;
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler;
import net.devopssolutions.demo.ws.rpc.RpcMethods;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Slf4j
@Component
@RpcMethod(RpcMethods.HELLO)
public class HelloWordHandler implements RpcMethodHandler {

    @Override
    public void handle(String id, Object params, Principal user) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.HELLO, params);
    }

}
