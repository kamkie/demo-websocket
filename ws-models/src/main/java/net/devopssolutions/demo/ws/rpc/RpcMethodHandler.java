package net.devopssolutions.demo.ws.rpc;

import java.security.Principal;

public interface RpcMethodHandler {

    void handle(String id, Object params, Principal user);
}
