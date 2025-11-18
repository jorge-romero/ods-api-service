package org.opendevstack.apiservice.core.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testDefaultConstructorSetsTimestamp() {
        // When
        ApiResponse<String> response = new ApiResponse<>();

        // Then
        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)));
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }

    @Test
    void testParameterizedConstructor() {
        // Given
        String data = "test data";
        String message = "test message";

        // When
        ApiResponse<String> response = new ApiResponse<>(true, data, message);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessFactoryMethod() {
        // Given
        String data = "success data";

        // When
        ApiResponse<String> response = ApiResponse.success(data);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSuccessFactoryMethodWithMessage() {
        // Given
        String data = "success data";
        String message = "Operation successful";

        // When
        ApiResponse<String> response = ApiResponse.success(data, message);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testErrorFactoryMethod() {
        // Given
        String errorMessage = "An error occurred";

        // When
        ApiResponse<String> response = ApiResponse.error(errorMessage);

        // Then
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(errorMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        // Given
        ApiResponse<Integer> response = new ApiResponse<>();
        Integer data = 42;
        String message = "Test message";
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        response.setSuccess(true);
        response.setData(data);
        response.setMessage(message);
        response.setTimestamp(timestamp);

        // Then
        assertTrue(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testSuccessWithNullData() {
        // When
        ApiResponse<String> response = ApiResponse.success(null);

        // Then
        assertTrue(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getMessage());
    }

    @Test
    void testErrorWithNullMessage() {
        // When
        ApiResponse<String> response = ApiResponse.error(null);

        // Then
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getMessage());
    }

    @Test
    void testGenericTypeWithComplexObject() {
        // Given
        class ComplexObject {
            private String field1;
            private int field2;

            ComplexObject(String field1, int field2) {
                this.field1 = field1;
                this.field2 = field2;
            }

            String getField1() {
                return field1;
            }

            int getField2() {
                return field2;
            }
        }

        ComplexObject complexData = new ComplexObject("test", 123);

        // When
        ApiResponse<ComplexObject> response = ApiResponse.success(complexData, "Created successfully");

        // Then
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals("test", response.getData().getField1());
        assertEquals(123, response.getData().getField2());
        assertEquals("Created successfully", response.getMessage());
    }

    @Test
    void testSuccessFalseConstructor() {
        // Given
        String data = "data";
        String message = "failed message";

        // When
        ApiResponse<String> response = new ApiResponse<>(false, data, message);

        // Then
        assertFalse(response.isSuccess());
        assertEquals(data, response.getData());
        assertEquals(message, response.getMessage());
    }

    @Test
    void testTimestampIsSetOnConstruction() {
        // Given
        LocalDateTime before = LocalDateTime.now();

        // When
        ApiResponse<String> response = new ApiResponse<>(true, "data", "message");

        // Then
        LocalDateTime after = LocalDateTime.now();
        assertTrue(response.getTimestamp().isAfter(before) || response.getTimestamp().isEqual(before));
        assertTrue(response.getTimestamp().isBefore(after) || response.getTimestamp().isEqual(after));
    }

    @Test
    void testResponseWithEmptyString() {
        // When
        ApiResponse<String> response = ApiResponse.success("", "Empty data");

        // Then
        assertTrue(response.isSuccess());
        assertEquals("", response.getData());
        assertEquals("Empty data", response.getMessage());
    }

    @Test
    void testMultipleResponsesHaveTimestamps() {
        // Given
        ApiResponse<String> response1 = new ApiResponse<>();
        ApiResponse<String> response2 = new ApiResponse<>();

        // Then
        assertNotNull(response1.getTimestamp());
        assertNotNull(response2.getTimestamp());
        // Both timestamps should be close to now
        assertTrue(response1.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
        assertTrue(response2.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)));
    }
}
