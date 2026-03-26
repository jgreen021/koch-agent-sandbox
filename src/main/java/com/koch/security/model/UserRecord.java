package com.koch.security.model;

import java.util.Set;

public record UserRecord(
    Long id,
    String username,
    String password,
    Set<Role> roles
) {}
