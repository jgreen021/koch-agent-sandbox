package com.koch.anomaly;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe cache registry for simulator to fetch Kilns without hitting DB.
 * Tracks connected SSE subscribers natively.
 */
@Service
public class KilnSimulationRegistry {

    private static final Logger log = LoggerFactory.getLogger(KilnSimulationRegistry.class);
    private final KilnRepository kilnRepository;

    // Cache to hold Active Kilns mapped by UUID
    private final ConcurrentHashMap<UUID, KilnCacheEntry> activeKilnsCache = new ConcurrentHashMap<>();

    // Inner class holding Kiln Entity and the Subscriber Count
    public static class KilnCacheEntry {
        public final Kiln kiln;
        public final AtomicInteger subscriberCount;

        public KilnCacheEntry(Kiln kiln) {
            this.kiln = kiln;
            this.subscriberCount = new AtomicInteger(0);
        }
    }

    public KilnSimulationRegistry(KilnRepository kilnRepository) {
        this.kilnRepository = kilnRepository;
    }

    @PostConstruct
    public void loadActiveKilns() {
        log.info("Loading active kilns into Simulation Registry...");
        List<Kiln> kilns = kilnRepository.findByIsActiveTrue();
        kilns.forEach(kiln -> activeKilnsCache.put(kiln.getId(), new KilnCacheEntry(kiln)));
        log.info("Loaded {} active kilns.", kilns.size());
    }

    public void updateKilnInCache(Kiln kiln) {
        if (Boolean.TRUE.equals(kiln.getIsActive())) {
            activeKilnsCache.compute(kiln.getId(), (id, existing) -> {
                if (existing != null) {
                    // Update object but keep existing subscriber count
                    KilnCacheEntry entry = new KilnCacheEntry(kiln);
                    entry.subscriberCount.set(existing.subscriberCount.get());
                    return entry;
                }
                return new KilnCacheEntry(kiln);
            });
        } else {
            removeKilnFromCache(kiln.getId());
        }
    }

    public void removeKilnFromCache(UUID kilnId) {
        activeKilnsCache.remove(kilnId);
    }

    public void incrementSubscriber(UUID kilnId) {
        KilnCacheEntry entry = activeKilnsCache.get(kilnId);
        if (entry != null) {
            entry.subscriberCount.incrementAndGet();
            log.info("Subscriber added to {}. Total: {}", kilnId, entry.subscriberCount.get());
        } else {
            log.warn("Attempted to subscribe to inactive or unknown kiln: {}", kilnId);
        }
    }

    public void decrementSubscriber(UUID kilnId) {
        KilnCacheEntry entry = activeKilnsCache.get(kilnId);
        if (entry != null) {
            entry.subscriberCount.decrementAndGet();
            log.info("Subscriber removed from {}. Total: {}", kilnId, entry.subscriberCount.get());
        }
    }

    public ConcurrentHashMap<UUID, KilnCacheEntry> getCache() {
        return activeKilnsCache;
    }
}
