import { useEffect, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useAppStore } from '../store/useAppStore';
import axios from 'axios';

export type ConnectionState = 'Empty' | 'Loading' | 'Connected' | 'Reconnecting';

export interface AssetSensorReading {
    readingId?: number;
    assetId: string;
    sensorType: string;
    readingValue: number;
    uom: string;
    timestamp: string;
    status: string;
}

export function useMonitoring(kilnId: string | undefined, token: string | undefined) {
    const queryClient = useQueryClient();
    const setToken = useAppStore(state => state.setToken);
    const [connectionState, setConnectionState] = useState<ConnectionState>('Empty');

    useEffect(() => {
        if (!kilnId || !token) {
            setConnectionState('Empty');
            return;
        }

        let eventSource: EventSource | null = null;
        let reconnectTimeout: ReturnType<typeof setTimeout>;
        let watchdogInterval: ReturnType<typeof setInterval>;
        let lastActivityTime = Date.now();
        let isInitialLoad = true;

        const connect = async () => {
            if (eventSource) eventSource.close();
            
            if (isInitialLoad) {
                setConnectionState('Loading');
                isInitialLoad = false;
            } else {
                setConnectionState('Reconnecting');
            }

            // Include token in query param due to EventSource limitations
            eventSource = new EventSource(`/api/sensors/stream/${kilnId}?token=${encodeURIComponent(token)}`);

            eventSource.onopen = () => {
                lastActivityTime = Date.now();
                setConnectionState('Connected');
            };

            eventSource.onmessage = (event) => {
                lastActivityTime = Date.now();
                setConnectionState('Connected');
                try {
                    const reading: AssetSensorReading = JSON.parse(event.data);
                    // Surgical update to the TanStack cache
                    queryClient.setQueryData(['telemetry', kilnId], reading);
                    
                    // Also append to historical log limited to 50 items if treating it as a list
                    queryClient.setQueryData<AssetSensorReading[]>(['telemetry-log', kilnId], (old = []) => {
                        return [reading, ...old].slice(0, 50);
                    });
                } catch (e) {
                    console.error("Failed to parse SSE event data:", event.data);
                }
            };

            eventSource.onerror = async () => {
                setConnectionState('Reconnecting');
                if (eventSource) {
                    eventSource.close();
                    eventSource = null;
                }
                
                // AUTH PROBE: Check if the failure is due to an invalid token
                try {
                    await axios.get('/api/auth/me', { headers: { Authorization: `Bearer ${token}` } });
                    // If we reached here, the token is fine, likely a server restart or network issue.
                    clearTimeout(reconnectTimeout);
                    reconnectTimeout = setTimeout(connect, 3000);
                } catch (err: any) {
                    if (err.response?.status === 401) {
                        console.error("JWT token invalidated. Redirecting to login.");
                        setToken(undefined); // This triggers the Dashboard redirect
                    } else {
                        // Generic network error, retry
                        clearTimeout(reconnectTimeout);
                        reconnectTimeout = setTimeout(connect, 5000);
                    }
                }
            };
        };

        // Watchdog: Server pings every 15s. If we hear nothing for 30s, the connection is zombie.
        watchdogInterval = setInterval(() => {
            const silenceDuration = Date.now() - lastActivityTime;
            if (silenceDuration > 35000) {
                if (eventSource) {
                    console.warn(`SSE Watchdog triggered for ${kilnId} due to silence.`);
                    connect();
                }
            }
        }, 15000);

        connect();

        return () => {
            clearTimeout(reconnectTimeout);
            clearInterval(watchdogInterval);
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [kilnId, token, queryClient, setToken]);

    return { connectionState };
}
