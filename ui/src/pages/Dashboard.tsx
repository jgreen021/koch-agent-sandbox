import React from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButtons, IonButton, IonIcon, useIonRouter } from '@ionic/react';
import { useQuery } from '@tanstack/react-query';
import { useMonitoring, AssetSensorReading, ConnectionState } from '../hooks/useMonitoring';
import { useAppStore } from '../store/useAppStore';
import { moonOutline, sunnyOutline, logOutOutline } from 'ionicons/icons';

const StatusBadge: React.FC<{ state: ConnectionState }> = React.memo(({ state }) => {
    let colorClass = "bg-gray-500";
    if (state === 'Connected') colorClass = "bg-green-500 animate-pulse";
    if (state === 'Reconnecting' || state === 'Loading') colorClass = "bg-yellow-500 animate-pulse";
    if (state === 'Empty') colorClass = "bg-red-500";

    return (
        <span className="flex items-center space-x-2 mr-4">
            <span className={`h-3 w-3 rounded-full ${colorClass}`}></span>
            <span className="text-sm font-medium">{state}</span>
        </span>
    );
});

const Dashboard: React.FC = () => {
    const token = useAppStore(state => state.token);
    const setToken = useAppStore(state => state.setToken);
    const darkMode = useAppStore(state => state.darkMode);
    const toggleDarkMode = useAppStore(state => state.toggleDarkMode);
    const router = useIonRouter();

    const handleLogout = () => {
        setToken(undefined);
    };

    const kilnId = 'KILN-01';
    const { connectionState } = useMonitoring(kilnId, token);

    // Retrieve live telemetry from the Tanstack Query cache
    const { data: telemetry } = useQuery<AssetSensorReading | null>({
        queryKey: ['telemetry', kilnId],
        queryFn: () => Promise.resolve(null), // Surgical updates are pushed via useMonitoring
        staleTime: Infinity,
        gcTime: Infinity,
    });


    let tempClass = "text-green-500";
    const value = telemetry?.readingValue || 0;
    
    // Explicitly follow the thresholds defined in the visual legend
    if (value >= 250 && value < 280) {
        tempClass = "text-yellow-500 font-bold";
    } else if (value >= 280) {
        tempClass = "text-red-500 font-bold animate-pulse";
    }

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar color="dark" role="banner">
                    <IonTitle>Real-Time Kiln Telemetry</IonTitle>
                    <IonButtons slot="end">
                        <div role="status" aria-live="polite">
                            <StatusBadge state={connectionState} />
                        </div>
                        <IonButton onClick={toggleDarkMode} aria-label={`Switch to ${darkMode ? 'light' : 'dark'} mode`}>
                            <IonIcon icon={darkMode ? sunnyOutline : moonOutline} />
                        </IonButton>
                        <IonButton onClick={handleLogout} className="ml-2" aria-label="Sign out of dashboard">
                            <IonIcon icon={logOutOutline} />
                        </IonButton>
                    </IonButtons>
                </IonToolbar>
            </IonHeader>
            <IonContent className="bg-gray-100 dark:bg-gray-900 transition-colors duration-300">
                <main className="flex flex-col items-center justify-center p-8 h-full space-y-6">
                    <section className={`p-8 rounded-2xl shadow-xl w-full max-w-md ${darkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900'}`} aria-labelledby="asset-title">
                        <header className="border-b pb-2 border-gray-200 dark:border-gray-700 mb-4">
                            <h2 id="asset-title" className="text-2xl font-semibold">
                                Asset: {kilnId}
                            </h2>
                        </header>
                        
                        <div 
                            className="flex flex-col items-center justify-center p-6 border-4 border-dashed rounded-full border-gray-300 dark:border-gray-600 mb-6 w-48 h-48 mx-auto"
                            role="timer"
                            aria-label={`Current temperature for ${kilnId}`}
                        >
                            <span className="text-gray-500 dark:text-gray-400 text-sm font-medium uppercase tracking-widest">Temperature</span>
                            <span 
                                className={`text-5xl my-2 ${tempClass} transition-colors duration-500`}
                                aria-live="polite"
                                aria-atomic="true"
                            >
                                {telemetry?.readingValue ? telemetry.readingValue.toFixed(1) : '--'}
                            </span>
                            <span className="text-xl font-medium text-gray-500">°{telemetry?.uom || 'C'}</span>
                        </div>

                        {/* Status Legend */}
                        <div className="flex justify-center space-x-6 mb-8 text-xs py-3 rounded-xl" role="complementary" aria-label="Temperature thresholds legend">
                            <div className="flex flex-col items-center space-y-1" aria-label="Normal: less than 250 degrees Celsius">
                                <div className="flex items-center space-x-2">
                                    <span className="w-3 h-3 rounded-full bg-green-500 inline-block shadow-sm"></span>
                                    <span className="text-gray-800 dark:text-gray-300 font-semibold tracking-wide uppercase">Normal</span>
                                </div>
                                <span className="text-gray-600 dark:text-gray-400 font-medium font-mono text-[11px]" aria-hidden="true">&lt;250°C</span>
                            </div>
                            
                            <div className="w-px h-8 bg-gray-300 dark:bg-gray-600" aria-hidden="true"></div>
                            
                            <div className="flex flex-col items-center space-y-1" aria-label="Warning: between 250 and 280 degrees Celsius">
                                <div className="flex items-center space-x-2">
                                    <span className="w-3 h-3 rounded-full bg-yellow-500 inline-block shadow-sm"></span>
                                    <span className="text-gray-800 dark:text-gray-300 font-semibold tracking-wide uppercase">Warning</span>
                                </div>
                                <span className="text-gray-600 dark:text-gray-400 font-medium font-mono text-[11px]" aria-hidden="true">250-280°C</span>
                            </div>

                            <div className="w-px h-8 bg-gray-300 dark:bg-gray-600" aria-hidden="true"></div>

                            <div className="flex flex-col items-center space-y-1" aria-label="Critical: 280 degrees Celsius or more">
                                <div className="flex items-center space-x-2">
                                    <span className="w-3 h-3 rounded-full bg-red-500 animate-pulse inline-block shadow-sm"></span>
                                    <span className="text-gray-800 dark:text-gray-300 font-semibold tracking-wide uppercase">Critical</span>
                                </div>
                                <span className="text-gray-600 dark:text-gray-400 font-medium font-mono text-[11px]" aria-hidden="true">&ge;280°C</span>
                            </div>
                        </div>

                        <footer className="text-center">
                            <p className="text-sm text-gray-500 dark:text-gray-400">
                                Last Update: {telemetry?.timestamp ? new Date(telemetry.timestamp).toLocaleTimeString() : 'Waiting for connection...'}
                            </p>
                        </footer>
                    </section>
                </main>
            </IonContent>
        </IonPage>
    );
};

export default Dashboard;
