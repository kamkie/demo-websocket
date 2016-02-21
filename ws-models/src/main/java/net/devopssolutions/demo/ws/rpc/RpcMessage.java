package net.devopssolutions.demo.ws.rpc;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString(of = {"id", "method", "type"})
public class RpcMessage<I, O> {

    private String id;
    private String method;
    private LocalDateTime created;
    private I params;
    private O response;
    private RpcError rpcError;
    private RpcType type;

}
