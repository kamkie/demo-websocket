package net.devopssolutions.demo.ws.rpc;

import java.time.LocalDateTime;

public class RpcMessage<I, O> {

    private String id;
    private String method;
    private LocalDateTime created;
    private I params;
    private O response;
    private RpcError rpcError;
    private RpcType type;

    @java.beans.ConstructorProperties({"id", "method", "created", "params", "response", "rpcError", "type"})
    RpcMessage(String id, String method, LocalDateTime created, I params, O response, RpcError rpcError, RpcType type) {
        this.id = id;
        this.method = method;
        this.created = created;
        this.params = params;
        this.response = response;
        this.rpcError = rpcError;
        this.type = type;
    }

    public static RpcMessageBuilder builder() {
        return new RpcMessageBuilder();
    }

    public String getId() {
        return this.id;
    }

    public String getMethod() {
        return this.method;
    }

    public LocalDateTime getCreated() {
        return this.created;
    }

    public I getParams() {
        return this.params;
    }

    public O getResponse() {
        return this.response;
    }

    public RpcError getRpcError() {
        return this.rpcError;
    }

    public RpcType getType() {
        return this.type;
    }

    public String toString() {
        return "net.devopssolutions.demo.ws.rpc.RpcMessage(id=" + this.getId() + ", method=" + this.getMethod() + ", type=" + this.getType() + ")";
    }

    public static class RpcMessageBuilder<I, O> {
        private String id;
        private String method;
        private LocalDateTime created;
        private I params;
        private O response;
        private RpcError rpcError;
        private RpcType type;

        RpcMessageBuilder() {
        }

        public RpcMessage.RpcMessageBuilder<I, O> id(String id) {
            this.id = id;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> method(String method) {
            this.method = method;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> created(LocalDateTime created) {
            this.created = created;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> params(I params) {
            this.params = params;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> response(O response) {
            this.response = response;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> rpcError(RpcError rpcError) {
            this.rpcError = rpcError;
            return this;
        }

        public RpcMessage.RpcMessageBuilder<I, O> type(RpcType type) {
            this.type = type;
            return this;
        }

        public RpcMessage<I, O> build() {
            return new RpcMessage<>(id, method, created, params, response, rpcError, type);
        }

        public String toString() {
            return "net.devopssolutions.demo.ws.rpc.RpcMessage.RpcMessageBuilder(id=" + this.id + ", method=" + this.method + ", created=" + this.created + ", params=" + this.params + ", response=" + this.response + ", rpcError=" + this.rpcError + ", type=" + this.type + ")";
        }
    }
}
