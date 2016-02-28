package net.devopssolutions.demo.ws.rpc

class RpcError constructor(val reason: String, val message: String, val throwable: Throwable) {
}
