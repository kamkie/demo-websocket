package net.devopssolutions.demo.ws.rpc

import java.time.LocalDateTime

data class RpcMessage<I, O>(
        val id: String,
        val method: String,
        val created: LocalDateTime,
        val params: I? = null,
        val response: O? = null,
        val rpcError: RpcError? = null,
        val type: RpcType) {
}
