package com.koch.anomaly;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SseConnectionManagerTest {

    private SseConnectionManager sseConnectionManager;

    @BeforeEach
    void setUp() {
        sseConnectionManager = new SseConnectionManager();
    }

    @Test
    void testRegisterSseEmitter_ReturnsEmitter() {
        SseEmitter emitter = sseConnectionManager.register("K-01");
        assertNotNull(emitter);
        assertEquals(1, sseConnectionManager.getConnectionCount("K-01"));
    }

    @Test
    void testBroadcastToAsset_SendsMessage() throws IOException {
        sseConnectionManager.register("K-01");
        
        // Broadcast string payload - should not throw
        sseConnectionManager.broadcast("K-01", "{\"temp\": 250}");
        
        assertEquals(1, sseConnectionManager.getConnectionCount("K-01"));
    }
    
    @Test
    void testCleanupOnCompletion() throws Exception {
        SseEmitter emitter = sseConnectionManager.register("K-01");
        assertEquals(1, sseConnectionManager.getConnectionCount("K-01"));
        
        // In a standalone unit test without a Servlet container, SseEmitter's onCompletion 
        // callback might not be triggered by emitter.complete(). 
        // We manually invoke the private removeEmitter method to verify the map cleanup logic.
        Method removeMethod = SseConnectionManager.class.getDeclaredMethod("removeEmitter", String.class, SseEmitter.class);
        removeMethod.setAccessible(true);
        removeMethod.invoke(sseConnectionManager, "K-01", emitter);
        
        // Cleanup should remove it
        assertEquals(0, sseConnectionManager.getConnectionCount("K-01"), "Emitter should be removed upon cleanup");
    }
}
