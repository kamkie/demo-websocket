package net.devopssolutions.demo.ws.rpc;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RpcError {
    private String reason;
    private String message;
    private Throwable throwable;
}
