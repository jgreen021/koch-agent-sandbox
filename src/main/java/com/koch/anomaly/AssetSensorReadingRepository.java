package com.koch.anomaly;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssetSensorReadingRepository extends JpaRepository<AssetSensorReadingEntity, Integer> {
    List<AssetSensorReadingEntity> findTop5ByOrderByTimestampDesc();
}
