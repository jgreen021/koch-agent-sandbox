package com.koch.anomaly;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KilnService {

    private final KilnRepository kilnRepository;
    private final KilnSimulationRegistry registry;

    public KilnService(KilnRepository kilnRepository, KilnSimulationRegistry registry) {
        this.kilnRepository = kilnRepository;
        this.registry = registry;
    }

    public List<KilnResponse> getAllKilns() {
        return kilnRepository.findAll().stream()
                .map(k -> new KilnResponse(
                        k.getId(), k.getName(), k.getType(), k.getIsActive(),
                        k.getBaselineTemp(), k.getWarningTemp(), k.getCriticalTemp(),
                        k.getStateDurationSeconds(), k.getWarningProbability(),
                        k.getCriticalProbability(),
                        1.0 - (k.getWarningProbability() + k.getCriticalProbability())
                )).collect(Collectors.toList());
    }

    @Transactional
    public KilnResponse createKiln(KilnRequest request) {
        Kiln kiln = new Kiln();
        mapRequestToEntity(request, kiln);
        Kiln saved = kilnRepository.save(kiln);
        registry.updateKilnInCache(saved);
        return KilnResponse.fromRequest(saved.getId(), request);
    }

    @Transactional
    public KilnResponse updateKiln(UUID id, KilnRequest request) {
        Kiln kiln = kilnRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Kiln not found"));
        mapRequestToEntity(request, kiln);
        Kiln saved = kilnRepository.save(kiln);
        registry.updateKilnInCache(saved);
        // Note: Future feature could push config_updated SSE event here
        return KilnResponse.fromRequest(saved.getId(), request);
    }

    @Transactional
    public void deleteKiln(UUID id) {
        kilnRepository.deleteById(id);
        registry.removeKilnFromCache(id);
    }

    private void mapRequestToEntity(KilnRequest request, Kiln kiln) {
        kiln.setName(request.name());
        kiln.setType(request.type());
        kiln.setIsActive(request.isActive());
        kiln.setBaselineTemp(request.baselineTemp());
        kiln.setWarningTemp(request.warningTemp());
        kiln.setCriticalTemp(request.criticalTemp());
        kiln.setStateDurationSeconds(request.stateDurationSeconds());
        kiln.setWarningProbability(request.warningProbability());
        kiln.setCriticalProbability(request.criticalProbability());
    }
}
