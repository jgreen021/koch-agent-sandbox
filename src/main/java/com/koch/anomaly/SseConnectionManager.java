package com.koch.anomaly;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(SseConnectionManager.class);
    
    // Maps Kiln ID to a list of active SseEmitters
    private final ConcurrentHashMap<String, List<SseEmitter>> clients = new ConcurrentHashMap<>();

    public SseEmitter register(String assetId) {
        // Set timeout to 0 (no timeout) or let standard Spring Boot timeout apply (usually 30s)
        // Here we use 0 (infinite) and rely on the client or ping to maintain connection
        SseEmitter emitter = new SseEmitter(0L);

        clients.computeIfAbsent(assetId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(assetId, emitter));
        emitter.onTimeout(() -> removeEmitter(assetId, emitter));
        emitter.onError(e -> removeEmitter(assetId, emitter));

        logger.info("Registered SSE client for asset {}. Total clients for asset: {}", assetId, clients.get(assetId).size());
        return emitter;
    }

    public void broadcast(String assetId, String payload) {
        List<SseEmitter> assetClients = clients.get(assetId);
        if (assetClients != null && !assetClients.isEmpty()) {
            assetClients.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("message").data(payload));
                } catch (IOException e) {
                    logger.debug("Failed to send message to SSE client. Removing connection.", e);
                    emitter.completeWithError(e); // This will trigger removeEmitter via onError
                }
            });
        }
    }

    @org.springframework.scheduling.annotation.Scheduled(fixedRate = 15000)
    public void sendPing() {
        clients.values().forEach(assetClients -> {
            assetClients.forEach(emitter -> {
                try {
                    // Sending a comment line to keep the connection alive
                    emitter.send(SseEmitter.event().comment("ping"));
                } catch (IOException e) {
                    logger.debug("Ping failed. Client disconnected.");
                    emitter.completeWithError(e);
                }
            });
        });
    }

    private void removeEmitter(String assetId, SseEmitter emitter) {
        List<SseEmitter> assetClients = clients.get(assetId);
        if (assetClients != null) {
            assetClients.remove(emitter);
            logger.info("Removed SSE client for asset {}. Remaining clients: {}", assetId, assetClients.size());
            if (assetClients.isEmpty()) {
                clients.remove(assetId);
            }
        }
    }

    public int getConnectionCount(String assetId) {
        List<SseEmitter> assetClients = clients.get(assetId);
        return assetClients != null ? assetClients.size() : 0;
    }
}
