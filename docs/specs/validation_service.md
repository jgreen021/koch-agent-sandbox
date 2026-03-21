# OpenSpec: Anomaly Validation Service

## Overview
The `AnomalyValidationService` is responsible for evaluating incoming asset data streams to determine if a specific reading constitutes a statistical anomaly based on recent historical performance.

## Core Logic Definition
An individual `Reading` is formally classified as an **anomaly** if its `ReadingValue` significantly deviates from the established baseline of the asset.

### The 20% Threshold Rule
A reading is considered anomalous if:
`ReadingValue` is **strictly greater than 20% higher or lower** than the mathematical average of the **last 10 readings** recorded for that specific `AssetID`.

### Edge Cases & Constraints
1. **Insufficient Historical Data**: If an asset has fewer than 10 historical readings in the database, the service should immediately return `false` (insufficient baseline, unable to confirm anomaly).
2. **Current Reading Exclusion**: The "last 10 readings" must *not* include the current reading being evaluated.
3. **Exact 20%**: A reading that is exactly 20.0% higher or lower than the average is *not* an anomaly. The deviation must be strictly greater than `> 20%`.
