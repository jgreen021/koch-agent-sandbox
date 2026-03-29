import React, { useState, useEffect } from 'react';
import { IonContent, IonHeader, IonPage, IonTitle, IonToolbar, IonButton, IonItem, IonLabel, IonInput, IonInputPasswordToggle, IonText, useIonRouter, useIonAlert, useIonToast } from '@ionic/react';
import axios from 'axios';
import { useAppStore } from '../store/useAppStore';

const Login: React.FC = () => {
    const savedUsername = useAppStore(state => state.savedUsername);
    const setSavedUsername = useAppStore(state => state.setSavedUsername);
    const [username, setUsername] = useState(savedUsername || '');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const setToken = useAppStore(state => state.setToken);
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

    const handleLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        try {
            const response = await axios.post('/api/auth/login', { username, password });
            if (response.data && response.data.accessToken) {
                setSavedUsername(username);
                setToken(response.data.accessToken);
                router.push('/dashboard', 'root');
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'Login failed. Check your credentials.');
        }
    };

    return (
        <IonPage className={darkMode ? 'dark' : ''}>
            <IonHeader>
                <IonToolbar color="dark">
                    <IonTitle>Koch Anomaly Dashboard Login</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="bg-gray-100 dark:bg-gray-900 ion-padding transition-colors duration-300">
                <div className="flex flex-col items-center justify-center p-8 h-full space-y-6">
                    <div className={`p-8 rounded-2xl shadow-xl w-full max-w-md ${darkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900 border border-gray-100'}`}>
                        <h2 className="text-2xl font-semibold mb-6 text-center border-b pb-4 border-gray-200 dark:border-gray-700">
                            Operator Sign In
                        </h2>

                        <form onSubmit={handleLogin} className="space-y-4">
                            <IonInput
                                value={username}
                                onIonChange={e => setUsername(e.detail.value!)}
                                required 
                                label="Username"
                                labelPlacement="stacked"
                                fill="outline" 
                                className="mb-4"
                                style={{ '--background': 'transparent', '--color': darkMode ? '#f3f4f6' : '#111827' }}
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
                            >
                                <IonInputPasswordToggle slot="end" />
                            </IonInput>

                            {error && (
                                <IonText color="danger">
                                    <p className="text-sm font-medium text-red-500 mt-2">{error}</p>
                                </IonText>
                            )}

                            <IonButton expand="block" type="submit" className="mt-6" fill="solid" size="large">
                                Connect to Telemetry Stream
                            </IonButton>

                            <div className="text-center mt-4 pt-4 border-t border-gray-200 dark:border-gray-700">
                                <IonButton
                                    fill="clear"
                                    size="small"
                                    className="text-gray-500"
                                    onClick={() => {
                                        presentAlert({
                                            header: 'Reset Password',
                                            message: 'Enter your username to request a reset link.',
                                            inputs: [{ name: 'resetUsername', type: 'text', placeholder: 'Username', value: username }],
                                            buttons: [
                                                { text: 'Cancel', role: 'cancel' },
                                                {
                                                    text: 'Send Reset Link',
                                                    handler: async (data) => {
                                                        try {
                                                            await axios.post('/api/auth/forgot-password', { username: data.resetUsername });
                                                            // For security, APIs usually return 200 even if the user doesn't exist
                                                            presentToast({ message: 'Password reset instructions sent (if account exists).', duration: 3000, color: 'success' });
                                                        } catch (err: any) {
                                                            const errMsg = err.response?.data?.message || 'Failed to send reset request.';
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
                </div>
            </IonContent>
        </IonPage>
    );
};

export default Login;
