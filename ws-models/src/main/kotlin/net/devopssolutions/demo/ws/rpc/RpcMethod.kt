package net.devopssolutions.demo.ws.rpc

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RpcMethod(val value: RpcMethods)
