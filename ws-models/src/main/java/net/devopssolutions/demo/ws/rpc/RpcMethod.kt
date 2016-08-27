package net.devopssolutions.demo.ws.rpc

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class RpcMethod(val value: String)
