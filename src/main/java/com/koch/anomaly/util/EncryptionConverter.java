package com.koch.anomaly.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Converter
@Component
public class EncryptionConverter implements AttributeConverter<Double, String> {

    private static final String ALGORITHM = "AES";
    private final SecretKeySpec keySpec;

    public EncryptionConverter(@Value("${reading-value-key:default-encryption-key-for-dev-32b}") String base64Key) {
        // Ensure key is 32 bytes for AES-256
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.keySpec = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    @Override
    public String convertToDatabaseColumn(Double attribute) {
        if (attribute == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(String.valueOf(attribute).getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error encrypting value", e);
        }
    }

    @Override
    public Double convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return Double.valueOf(new String(decrypted));
        } catch (GeneralSecurityException | NumberFormatException e) {
            throw new RuntimeException("Error decrypting value", e);
        }
    }
}
