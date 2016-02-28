package net.devopssolutions.demo.ws.rpc

import java.security.Principal

interface RpcMethodHandler {

    fun handle(id: String, params: Any, user: Principal)
}
