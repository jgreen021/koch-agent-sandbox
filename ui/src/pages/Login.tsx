import React, { useState, useEffect } from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButton, IonItem, IonLabel, IonInput, IonInputPasswordToggle, IonText, useIonRouter, useIonAlert, useIonToast } from '@ionic/react';
import axios, { AxiosError } from 'axios';
import api from '../services/api';
import { useAppStore } from '../store/useAppStore';

interface ErrorResponse {
    message?: string;
}

const Login: React.FC = () => {
    const savedUsername = useAppStore(state => state.savedUsername);
    const setSavedUsername = useAppStore(state => state.setSavedUsername);
    const [username, setUsername] = useState(savedUsername || '');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const setTokens = useAppStore(state => state.setTokens);
    const setUserRole = useAppStore(state => state.setUserRole);
    const darkMode = useAppStore(state => state.darkMode);
    const router = useIonRouter();
    const [presentAlert] = useIonAlert();
    const [presentToast] = useIonToast();

    // Sync saved username when it loads
    useEffect(() => {
        if (savedUsername && !username) {
            setUsername(savedUsername);
        }
    }, [savedUsername]);

    // Helper to extract role and expiry from JWT token
    const extractClaimsFromToken = (token: string): { role: string | undefined, exp: number | undefined } => {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const payload = JSON.parse(window.atob(base64));
            
            // Handle both 'scope' (string) and 'roles' (array)
            const rawClaims = payload.scope || payload.roles || [];
            const claims = Array.isArray(rawClaims) ? rawClaims.join(' ') : String(rawClaims);
            
            let role = 'USER';
            // Case-insensitive/sub-string check for robustness (matches ROLE_ADMIN, GATEWAY_ADMIN, etc.)
            if (claims.includes('ADMIN') || claims.includes('GATEWAY')) role = 'ADMIN';
            else if (claims.includes('OPERATOR')) role = 'OPERATOR';
            
            return { role, exp: payload.exp };
        } catch (e) {
            console.error('Failed to decode token for claims', e);
            return { role: undefined, exp: undefined };
        }
    };

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        try {
            const response = await api.post('/api/auth/login', { username, password });
            if (response.data && response.data.accessToken) {
                setSavedUsername(username);
                const { accessToken, refreshToken, expiresIn } = response.data;
                
                // Extract claims from token
                const { role, exp } = extractClaimsFromToken(accessToken);
                
                // expiry is relative
                const absoluteExpiry = expiresIn ? Math.floor(Date.now() / 1000) + expiresIn : exp;
                setTokens(accessToken, refreshToken, absoluteExpiry);
                setUserRole(role);
                
                router.push('/dashboard', 'root');
            }
        } catch (err: unknown) {
            const axiosErr = err as AxiosError<ErrorResponse>;
            // Mask detailed errors to prevent user enumeration or sensitive leaks
            const msg = axiosErr.response?.data?.message || 'Login failed. Please verify your credentials.';
            setError(msg);
        }
    };

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar color="dark">
                    <IonTitle>Koch Anomaly Dashboard Login</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="bg-gray-100 dark:bg-gray-900 ion-padding transition-colors duration-300">
                <main className="flex flex-col items-center justify-center p-8 h-full space-y-6">
                    <div className={`p-8 rounded-2xl shadow-xl w-full max-w-md ${darkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900 border border-gray-100'}`} role="region" aria-labelledby="login-header">
                        <h2 id="login-header" className="text-2xl font-semibold mb-6 text-center border-b pb-4 border-gray-200 dark:border-gray-700">
                            Operator Sign In
                        </h2>

                        <form onSubmit={handleLogin} className="space-y-4" aria-describedby="login-info">
                            <p id="login-info" className="sr-only">Please enter your credentials to access the telemetry dashboard.</p>
                            
                            <IonInput
                                value={username}
                                onIonChange={e => setUsername(e.detail.value!)}
                                required 
                                label="Username"
                                labelPlacement="stacked"
                                fill="outline" 
                                className="mb-4"
                                style={{ '--background': 'transparent', '--color': darkMode ? '#f3f4f6' : '#111827' }}
                                aria-label="Enter your operator username"
                                autocomplete="username"
                            />

                            <IonInput
                                value={password}
                                type="password"
                                onIonInput={e => setPassword(e.detail.value!)}
                                required 
                                label="Password"
                                labelPlacement="stacked"
                                fill="outline" 
                                style={{ '--background': 'transparent', '--color': darkMode ? '#f3f4f6' : '#111827' }}
                                aria-label="Enter your password"
                                autocomplete="current-password"
                            >
                                <IonInputPasswordToggle slot="end" aria-label="Toggle password visibility" />
                            </IonInput>

                            <div aria-live="polite" className="min-h-[24px]">
                                {error && (
                                    <IonText color="danger">
                                        <p className="text-sm font-medium text-red-500 mt-2">{error}</p>
                                    </IonText>
                                )}
                            </div>

                            <IonButton expand="block" type="submit" className="mt-6" fill="solid" size="large" aria-label="Sign in to dashboard">
                                Connect to Telemetry Stream
                            </IonButton>

                            <div className="text-center mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                                <IonButton
                                    fill="clear"
                                    size="small"
                                    className="text-gray-500"
                                    aria-label="Forgot password request"
                                    onClick={() => {
                                        presentAlert({
                                            header: 'Reset Password',
                                            message: 'Enter your username to request a reset link.',
                                            inputs: [{ name: 'resetUsername', type: 'text', placeholder: 'Username', value: username, label: 'Username' }],
                                            buttons: [
                                                { text: 'Cancel', role: 'cancel' },
                                                {
                                                    text: 'Send Reset Link',
                                                     handler: async (data) => {
                                                        try {
                                                            await api.post('/api/auth/forgot-password', { username: data.resetUsername });
                                                            presentToast({ 
                                                                message: 'Reset token generated! Check server logs and visit /reset-password?token=<TOKEN>', 
                                                                duration: 8000, 
                                                                color: 'success' 
                                                            });
                                                        } catch (err: unknown) {
                                                            const axiosErr = err as AxiosError<ErrorResponse>;
                                                            const errMsg = axiosErr.response?.data?.message || 'Failed to send reset request.';
                                                            presentToast({ message: errMsg, duration: 3000, color: 'danger' });
                                                        }
                                                    }
                                                }
                                            ]
                                        });
                                    }}>
                                    Forgot Password?
                                </IonButton>
                            </div>
                        </form>
                    </div>
                </main>
            </IonContent>
        </IonPage>
    );
};

export default Login;
