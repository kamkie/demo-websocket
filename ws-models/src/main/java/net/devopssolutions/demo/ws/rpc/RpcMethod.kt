package net.devopssolutions.demo.ws.rpc

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE)
annotation class RpcMethod(val value: String)
