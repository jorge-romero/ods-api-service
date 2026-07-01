package org.opendevstack.apiservice.core.security.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base64OperationsTest {

    @Test
    void tectEncodeWhenNullInputReturnNull() {
        String testString = null;

        String encodedString = Base64Operations.encode(testString);

        assertEquals(null, encodedString);
    }

    @Test
    void tectEncodeWhenCorrectInputReturnEncodedResult() {
        String testString = "test;string";

        String encodedString = Base64Operations.encode(testString);

        assertEquals("dGVzdDtzdHJpbmc=", encodedString);
    }

    @Test
    void testDecodeWhenNullInputReturnNull() {
        String testString = null;

        String dencodedString = Base64Operations.decode(testString);

        assertEquals(null, dencodedString);
    }

    @Test
    void testDecodeWhenCorrectInputReturnDecodedResult() {
        String testString = "dGVzdDtzdHJpbmc=";

        String dencodedString = Base64Operations.decode(testString);

        assertEquals("test;string", dencodedString);
    }

    @Test
    void testDecodeWhenBadInputThrowException() {
        String testString = "NOT AN ENCODED STRING";

        assertThrows(IllegalArgumentException.class, () -> Base64Operations.decode(testString));
    }
}