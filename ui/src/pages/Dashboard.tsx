import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, useIonRouter, IonGrid, IonRow, IonCol, IonSpinner } from '@ionic/react';
import { useQuery } from '@tanstack/react-query';
import { useMonitoring, AssetSensorReading, ConnectionState } from '../hooks/useMonitoring';
import { useAppStore } from '../store/useAppStore';
import { moonOutline, sunnyOutline, logOutOutline, refreshOutline } from 'ionicons/icons';
import api from '../services/api';

interface Kiln {
    id: string;
    name: string;
    type: string;
    baselineTemp: number;
    warningTemp: number;
    criticalTemp: number;
}

const StatusBadge: React.FC<{ state: ConnectionState }> = React.memo(({ state }) => {
    let colorClass = "bg-gray-500";
    if (state === 'Connected') colorClass = "bg-green-500 animate-pulse";
    if (state === 'Reconnecting' || state === 'Loading') colorClass = "bg-yellow-500 animate-pulse";
    if (state === 'Empty') colorClass = "bg-red-500";

    return (
        <span className="flex items-center space-x-2">
            <span className={`h-2.5 w-2.5 rounded-full ${colorClass}`}></span>
            <span className="text-xs font-semibold uppercase tracking-tighter opacity-80">{state}</span>
        </span>
    );
});

const KilnCard: React.FC<{ kiln: Kiln; token: string; darkMode: boolean }> = ({ kiln, token, darkMode }) => {
    const { connectionState } = useMonitoring(kiln.name, token);

    // Retrieve live telemetry from the Tanstack Query cache specifically for this asset name
    const { data: telemetry } = useQuery<AssetSensorReading | null>({
        queryKey: ['telemetry', kiln.name],
        queryFn: () => Promise.resolve(null),
        staleTime: Infinity,
        gcTime: Infinity,
    });

    const value = telemetry?.readingValue || 0;
    let tempClass = "text-green-500";
    
    if (value >= kiln.warningTemp && value < kiln.criticalTemp) {
        tempClass = "text-yellow-500 font-bold";
    } else if (value >= kiln.criticalTemp) {
        tempClass = "text-red-500 font-bold animate-pulse";
    }

    return (
        <section className={`p-6 rounded-2xl shadow-lg border border-transparent hover:border-blue-500 transition-all duration-300 ${darkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'}`}>
            <header className="flex justify-between items-start mb-4 border-b border-gray-100 dark:border-gray-700 pb-2">
                <div>
                    <h3 className="text-xl font-bold">{kiln.name}</h3>
                    <p className="text-xs text-gray-400 uppercase tracking-widest">{kiln.type}</p>
                </div>
                <StatusBadge state={connectionState} />
            </header>
            
            <div className="flex flex-col items-center justify-center p-4 border-2 border-dashed rounded-full border-gray-200 dark:border-gray-700 w-36 h-36 mx-auto mb-4">
                <span className="text-[10px] text-gray-500 uppercase font-bold tracking-widest">Temperature</span>
                <span className={`text-4xl my-1 ${tempClass} transition-colors duration-500 tabular-nums`}>
                    {telemetry?.readingValue ? telemetry.readingValue.toFixed(1) : '--'}
                </span>
                <span className="text-sm font-bold text-gray-400">°{telemetry?.uom || 'C'}</span>
            </div>

            <footer className="text-center mt-2 opacity-60">
                <p className="text-[10px] truncate">
                    {telemetry?.timestamp ? `Updated: ${new Date(telemetry.timestamp).toLocaleTimeString()}` : 'Waiting for stream...'}
                </p>
            </footer>
        </section>
    );
};

const Dashboard: React.FC = () => {
    const token = useAppStore(state => state.token);
    const clearAuth = useAppStore(state => state.clearAuth);
    const darkMode = useAppStore(state => state.darkMode);
    const toggleDarkMode = useAppStore(state => state.toggleDarkMode);

    // Fetch the dynamic list of kilns from the DB
    const { data: kilns, isLoading, error, refetch } = useQuery<Kiln[]>({
        queryKey: ['kilns'],
        queryFn: async () => {
            const resp = await api.get('/api/kilns');
            return resp.data;
        },
        enabled: !!token
    });

    const handleLogout = () => {
        clearAuth();
    };

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar color="dark">
                    <IonTitle>Industrial Monitoring</IonTitle>
                    <IonButtons slot="end">
                        <IonButton onClick={() => refetch()} aria-label="Refresh list">
                            <IonIcon icon={refreshOutline} />
                        </IonButton>
                        <IonButton onClick={toggleDarkMode}>
                            <IonIcon icon={darkMode ? sunnyOutline : moonOutline} />
                        </IonButton>
                        <IonButton onClick={handleLogout} className="ml-2">
                            <IonIcon icon={logOutOutline} />
                        </IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonHeader>
            <IonContent className="bg-gray-50 dark:bg-gray-900 transition-colors duration-300">
                <IonGrid>
                    <IonRow>
                        <IonCol size="12" className="p-4">
                            <h2 className="text-sm font-black text-gray-400 uppercase tracking-[0.2em] mb-4 border-l-4 border-blue-500 pl-3">
                                Active Production Assets
                            </h2>
                        </IonCol>
                    </IonRow>
                    
                    {isLoading ? (
                        <div className="flex flex-col items-center justify-center p-20 w-full opacity-50">
                            <IonSpinner name="crescent" />
                            <p className="mt-4 text-xs font-bold uppercase tracking-widest">Hydrating Registry...</p>
                        </div>
                    ) : error ? (
                        <div className="p-10 text-center text-red-500 font-bold">
                            Error loading telemetry registry.
                        </div>
                    ) : (
                        <IonRow>
                            {kilns?.map((kiln) => (
                                <IonCol key={kiln.id} size="12" sizeMd="6" sizeLg="4" className="p-3">
                                    <KilnCard kiln={kiln} token={token || ''} darkMode={darkMode} />
                                </IonCol>
                            ))}
                            {kilns?.length === 0 && (
                                <IonCol size="12" className="text-center p-10 text-gray-500">
                                    No active kilns found in registry.
                                </IonCol>
                            )}
                        </IonRow>
                    )}
                </IonGrid>
            </IonContent>
        </IonPage>
    );
};

export default Dashboard;
