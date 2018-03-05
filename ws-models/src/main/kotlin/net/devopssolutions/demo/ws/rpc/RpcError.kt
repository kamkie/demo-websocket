package net.devopssolutions.demo.ws.rpc

import java.io.Serializable

data class RpcError constructor(
        val reason: String,
        val message: String,
        val throwable: Throwable
) : Serializable
