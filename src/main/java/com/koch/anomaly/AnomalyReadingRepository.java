package com.koch.anomaly;

import java.util.List;

public interface AnomalyReadingRepository {
    List<AnomalyReading> findTop10ByAssetIdOrderByTimestampDesc(String assetId);
}
