# OpenSpec: Anomaly Validation Service

## Overview
The `AnomalyValidationService` is responsible for evaluating incoming asset data streams to determine if a specific reading constitutes a statistical anomaly based on recent historical performance.

## Core Logic Definition
An individual `Reading` is formally classified as an **anomaly** if its `ReadingValue` significantly deviates from the established baseline of the asset.

### Threshold & Validation Rules
The service must evaluate incoming readings against the following rules, in this order of precedence:

* **The Cold Start Rule:** If an `AssetID` has fewer than 10 historical readings in the system, the service must bypass the anomaly check and return a status of `Insufficient Data`.
* **The Absolute Ceiling Override:** Regardless of historical averages, any `ReadingValue` exceeding `120.0` must immediately be flagged with a status of `Critical`.
* **The Warning Tier:** If the `ReadingValue` deviates between `15%` and `24.9%` from the 10-reading rolling average, it must be flagged with a status of `Warning`.
* **The Critical Tier:** If the `ReadingValue` deviates by `25%` or more from the 10-reading rolling average, it must be flagged with a status of `Critical`.

### Edge Cases & Constraints
1. **Current Reading Exclusion**: The "last 10 readings" must *not* include the current reading being evaluated.
