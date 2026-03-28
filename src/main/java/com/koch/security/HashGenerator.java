package com.koch.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String password = args.length > 0 ? args[0] : "password123";
        System.out.println("RESULT_HASH:" + encoder.encode(password));
    }
}
