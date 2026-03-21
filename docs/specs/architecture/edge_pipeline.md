# Edge Pipeline Architecture

## System Overview
This diagram illustrates the high-throughput IoT data pipeline. The `AnomalyValidationService` acts as an edge filter, sitting between the local factory Kafka stream and the centralized Azure SQL database to drop normal readings and drastically reduce cloud ingress costs.

## Data Flow Diagram

```text
       THE "EDGE" (Physically inside the Plant)
===================================================================

 [Kiln Sensor A]     [Kiln Sensor B]     [Kiln Sensor C]
        |                   |                   |  (1 reading/second)
        +-------------------+-------------------+
                            |
                            v
            +-------------------------------+
            |      LOCAL KAFKA CLUSTER      |  <-- High-throughput buffer.
            | Topic: 'kiln-sensor-readings' |      Holds data temporarily.
            +-------------------------------+
                            |
                            | (Consumed by our Spring Boot app)
                            v
            +-------------------------------+
            |  AnomalyValidationService     |  <-- THIS IS YOUR CODE.
            |  (The Edge Microservice)      |      Calculates 10-tick average.
            +-------------------------------+      Applies OpenSpec rules.
                  /                   \
                 /                     \
       [Status: NORMAL]          [Status: WARNING / CRITICAL]
              |                               |
              v                               | (Only anomalies pass through)
       (Data is Dropped)                      |
       Saves Cloud Costs!                     |
                                              |
===================================================================
       THE CLOUD (Microsoft Azure)            |
===================================================================
                                              v
                                +---------------------------+
                                |    AZURE SQL DATABASE     | <-- Low volume,
                                |  Table: 'anomaly_alerts'  |     high value data.
                                +---------------------------+
                                              |
                                              v
                                  [Plant Manager's Dashboard]