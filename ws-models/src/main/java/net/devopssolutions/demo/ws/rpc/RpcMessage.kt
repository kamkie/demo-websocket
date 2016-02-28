package net.devopssolutions.demo.ws.rpc

import java.time.LocalDateTime

data class RpcMessage<I, O>(val id: String, val method: String, val created: LocalDateTime, val params: I?, val response: O?, val rpcError: RpcError?, val type: RpcType) {

    class RpcMessageBuilder<I, O> internal constructor() {
        private var id: String? = null
        private var method: String? = null
        private var created: LocalDateTime? = null
        private var params: I? = null
        private var response: O? = null
        private var rpcError: RpcError? = null
        private var type: RpcType? = null

        fun id(id: String): RpcMessage.RpcMessageBuilder<I, O> {
            this.id = id
            return this
        }

        fun method(method: String): RpcMessage.RpcMessageBuilder<I, O> {
            this.method = method
            return this
        }

        fun created(created: LocalDateTime): RpcMessage.RpcMessageBuilder<I, O> {
            this.created = created
            return this
        }

        fun params(params: I): RpcMessage.RpcMessageBuilder<I, O> {
            this.params = params
            return this
        }

        fun response(response: O): RpcMessage.RpcMessageBuilder<I, O> {
            this.response = response
            return this
        }

        fun rpcError(rpcError: RpcError): RpcMessage.RpcMessageBuilder<I, O> {
            this.rpcError = rpcError
            return this
        }

        fun type(type: RpcType): RpcMessage.RpcMessageBuilder<I, O> {
            this.type = type
            return this
        }

        fun build(): RpcMessage<I, O> {
            return RpcMessage(id!!, method!!, created!!, params, response, rpcError, type!!)
        }

        override fun toString(): String {
            return "net.devopssolutions.demo.ws.rpc.RpcMessage.RpcMessageBuilder(id=" + this.id + ", method=" + this.method + ", created=" + this.created + ", params=" + this.params + ", response=" + this.response + ", rpcError=" + this.rpcError + ", type=" + this.type + ")"
        }
    }

    companion object {
        fun builder(): RpcMessageBuilder<Any, Any> {
            return RpcMessageBuilder()
        }
    }

}
