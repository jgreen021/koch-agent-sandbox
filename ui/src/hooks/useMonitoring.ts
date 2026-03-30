import { useEffect, useState, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useIonToast } from '@ionic/react';
import { useAppStore } from '../store/useAppStore';
import api from '../services/api';
import axios from 'axios';
import { cloudOfflineOutline, cloudDoneOutline } from 'ionicons/icons';

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
    const clearAuth = useAppStore(state => state.clearAuth);
    const [connectionState, setConnectionState] = useState<ConnectionState>('Empty');
    const [present] = useIonToast();
    const presentRef = useRef(present);
    const clearAuthRef = useRef(clearAuth);
    const mountedRef = useRef(true);

    // Keep refs in sync
    useEffect(() => {
        presentRef.current = present;
        clearAuthRef.current = clearAuth;
    }, [present, clearAuth]);

    useEffect(() => {
        mountedRef.current = true;
        if (!kilnId || !token) {
            setConnectionState(prev => prev !== 'Empty' ? 'Empty' : prev);
            return;
        }

        let eventSource: EventSource | null = null;
        let reconnectTimeout: ReturnType<typeof setTimeout>;
        let watchdogInterval: ReturnType<typeof setInterval>;
        let toastTimeout: ReturnType<typeof setTimeout>;
        let lastActivityTime = Date.now();
        let isInitialLoad = true;

        const connect = async () => {
            if (!mountedRef.current || !token) return;
            if (eventSource) eventSource.close();
            
            if (isInitialLoad) {
                setConnectionState('Loading');
                isInitialLoad = false;
            } else {
                setConnectionState('Reconnecting');
                // Wait 5s before showing reconnecting toast to avoid flickering
                clearTimeout(toastTimeout);
                toastTimeout = setTimeout(() => {
                    if (mountedRef.current) {
                        presentRef.current({
                            message: `Connection lost. Attempting to reconnect to ${kilnId}...`,
                            duration: 3000,
                            color: 'warning',
                            icon: cloudOfflineOutline,
                            position: 'bottom'
                        });
                    }
                }, 5000);
            }

            // Include token in query param due to EventSource limitations
            eventSource = new EventSource(`/api/sensors/stream/${kilnId}?token=${encodeURIComponent(token)}`);

            eventSource.onopen = () => {
                if (!mountedRef.current) {
                    eventSource?.close();
                    return;
                }
                lastActivityTime = Date.now();
                clearTimeout(toastTimeout);
                if (connectionState === 'Reconnecting') {
                    presentRef.current({
                        message: 'Connection restored.',
                        duration: 2000,
                        color: 'success',
                        icon: cloudDoneOutline,
                        position: 'bottom'
                    });
                }
                setConnectionState('Connected');
            };

            eventSource.onmessage = (event) => {
                if (!mountedRef.current) return;
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
                if (!mountedRef.current || !token) {
                    if (eventSource) {
                        eventSource.close();
                        eventSource = null;
                    }
                    return;
                }

                setConnectionState('Reconnecting');
                if (eventSource) {
                    eventSource.close();
                    eventSource = null;
                }
                
                // AUTH PROBE: Check if the failure is due to an invalid token
                try {
                    await api.get('/api/auth/me');
                    // If we reached here, the token is fine, likely a server restart or network issue.
                    if (mountedRef.current && token) {
                        clearTimeout(reconnectTimeout);
                        reconnectTimeout = setTimeout(connect, 3000);
                    }
                } catch (err: any) {
                    if (err.response?.status === 401) {
                        console.error("JWT token invalidated and refresh failed. Redirecting to login.");
                        if (mountedRef.current && token) {
                            clearAuthRef.current(); // This triggers the Dashboard redirect
                        }
                    } else if (mountedRef.current && token) {
                        // Generic network error, retry
                        clearTimeout(reconnectTimeout);
                        reconnectTimeout = setTimeout(connect, 5000);
                    }
                }
            };
        };

        // Watchdog: Server pings every 15s. If we hear nothing for 30s, the connection is zombie.
        watchdogInterval = setInterval(() => {
            if (!mountedRef.current) return;
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
            mountedRef.current = false;
            clearTimeout(reconnectTimeout);
            clearTimeout(toastTimeout);
            clearInterval(watchdogInterval);
            if (eventSource) {
                eventSource.close();
            }
        };
    }, [kilnId, token, queryClient]);

    return { connectionState };
}
