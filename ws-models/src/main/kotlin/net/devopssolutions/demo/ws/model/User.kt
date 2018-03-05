package net.devopssolutions.demo.ws.model

import java.security.Principal

data class User(val userName: String) : Principal {
    override fun getName(): String = userName
}
