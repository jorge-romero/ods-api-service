package org.opendevstack.apiservice.core.security.util;

import java.util.Base64;

public class Base64Operations {

    private Base64Operations() {}

    /**
     * Encodes a string value using Base64 encoding.
     *
     * @param value the string value to encode
     * @return the Base64 encoded string, or null if input is null
     */
    public static String encode(String value) {
        if (value == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(value.getBytes());
    }

    /**
     * Decodes a Base64 encoded string value.
     *
     * @param encodedValue the Base64 encoded string to decode
     * @return the decoded string, or null if input is null
     * @throws IllegalArgumentException if the input is not valid Base64
     */
    public static String decode(String encodedValue) {
        if (encodedValue == null) {
            return null;
        }
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encodedValue);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoded value: " + encodedValue, e);
        }
    }
}
