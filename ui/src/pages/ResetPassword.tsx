import React, { useState, useEffect } from 'react';
import { 
  IonContent, 
  IonHeader, 
  IonPage, 
  IonTitle, 
  IonToolbar, 
  IonButton, 
  IonItem, 
  IonLabel, 
  IonInput, 
  IonInputPasswordToggle, 
  IonText, 
  useIonRouter, 
  useIonToast 
} from '@ionic/react';
import { useLocation } from 'react-router-dom';
import axios, { AxiosError } from 'axios';
import api from '../services/api';
import { useAppStore } from '../store/useAppStore';

interface ErrorResponse {
    message?: string;
}

const ResetPassword: React.FC = () => {
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [token, setToken] = useState<string | null>(null);
    const darkMode = useAppStore(state => state.darkMode);
    const router = useIonRouter();
    const [presentToast] = useIonToast();
    const location = useLocation();

    useEffect(() => {
        const queryParams = new URLSearchParams(location.search);
        const t = queryParams.get('token');
        if (t) {
            setToken(t);
        } else {
            setError('Missing or invalid reset token. Request a new link.');
        }
    }, [location]);

    const handleReset = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');

        if (newPassword !== confirmPassword) {
            setError('Passwords do not match.');
            return;
        }

        if (newPassword.length < 8) {
            setError('Password must be at least 8 characters long.');
            return;
        }

        if (!token) {
            setError('No valid token found.');
            return;
        }

        try {
            await api.post('/api/auth/reset-password', { 
                token: token, 
                newPassword: newPassword 
            });
            
            presentToast({ 
                message: 'Password reset successfully. Please login with your new credentials.', 
                duration: 4000, 
                color: 'success' 
            });
            
            router.push('/login', 'root');
        } catch (err: unknown) {
            const axiosErr = err as AxiosError<ErrorResponse>;
            const msg = axiosErr.response?.data?.message || 'Failed to reset password. The token may be expired.';
            setError(msg);
        }
    };

    return (
        <IonPage>
            <IonHeader>
                <IonToolbar color="dark">
                    <IonTitle>Reset Your Password</IonTitle>
                </IonToolbar>
            </IonHeader>
            <IonContent className="bg-gray-100 dark:bg-gray-900 ion-padding transition-colors duration-300">
                <main className="flex flex-col items-center justify-center p-8 h-full space-y-6">
                    <div className={`p-8 rounded-2xl shadow-xl w-full max-w-md ${darkMode ? 'bg-gray-800 text-white' : 'bg-white text-gray-900 border border-gray-100'}`}>
                        <h2 className="text-2xl font-semibold mb-6 text-center border-b pb-4 border-gray-200 dark:border-gray-700">
                            Set New Password
                        </h2>

                        <form onSubmit={handleReset} className="space-y-4">
                            <IonInput
                                value={newPassword}
                                type="password"
                                onIonInput={e => setNewPassword(e.detail.value!)}
                                required 
                                label="New Password"
                                labelPlacement="stacked"
                                fill="outline" 
                                style={{ '--background': 'transparent', '--color': darkMode ? '#f3f4f6' : '#111827' }}
                                aria-label="Enter your new password"
                                autocomplete="new-password"
                            >
                                <IonInputPasswordToggle slot="end" aria-label="Toggle password visibility" />
                            </IonInput>

                            <IonInput
                                value={confirmPassword}
                                type="password"
                                onIonInput={e => setConfirmPassword(e.detail.value!)}
                                required 
                                label="Confirm New Password"
                                labelPlacement="stacked"
                                fill="outline" 
                                style={{ '--background': 'transparent', '--color': darkMode ? '#f3f4f6' : '#111827' }}
                                aria-label="Confirm your new password"
                                autocomplete="new-password"
                            >
                                <IonInputPasswordToggle slot="end" aria-label="Toggle password visibility" />
                            </IonInput>

                            <div aria-live="polite" className="min-h-[24px]">
                                {error && (
                                    <IonText color="danger">
                                        <p className="text-sm font-medium text-red-500 mt-2">{error}</p>
                                    </IonText>
                                )}
                                {!error && confirmPassword && newPassword !== confirmPassword && (
                                    <IonText color="danger">
                                        <p className="text-sm font-medium text-red-500 mt-2">Passwords do not match.</p>
                                    </IonText>
                                )}
                                {!error && confirmPassword && newPassword === confirmPassword && newPassword.length >= 8 && (
                                    <IonText color="success">
                                        <p className="text-sm font-medium text-green-500 mt-2">Passwords match!</p>
                                    </IonText>
                                )}
                            </div>

                            <IonButton 
                                expand="block" 
                                type="submit" 
                                className="mt-6" 
                                fill="solid" 
                                size="large" 
                                disabled={!token || newPassword !== confirmPassword || newPassword.length < 8}
                            >
                                Update Password
                            </IonButton>

                            <div className="text-center mt-4">
                                <IonButton fill="clear" size="small" onClick={() => router.push('/login', 'back')}>
                                    Back to Login
                                </IonButton>
                            </div>
                        </form>
                    </div>
                </main>
            </IonContent>
        </IonPage>
    );
};

export default ResetPassword;
