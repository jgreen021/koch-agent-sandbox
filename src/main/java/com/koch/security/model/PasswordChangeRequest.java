package com.koch.security.model;

public record PasswordChangeRequest(String oldPassword, String newPassword) {}
