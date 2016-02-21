package net.devopssolutions.demo.ws.model;

import lombok.Data;

import java.security.Principal;

@Data
public class User implements Principal{

    private String name;
}
