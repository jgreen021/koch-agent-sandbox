import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AppState {
    darkMode: boolean;
    toggleDarkMode: () => void;
    token: string | undefined;
    refreshToken: string | undefined;
    tokenExpiry: number | undefined;
    setToken: (token: string | undefined, expiry?: number) => void;
    setTokens: (accessToken: string, refreshToken: string, expiry?: number) => void;
    userRole: string | undefined;
    setUserRole: (role: string | undefined) => void;
    savedUsername: string;
    setSavedUsername: (username: string) => void;
    isTokenExpired: () => boolean;
    clearAuth: () => void;
}

export const useAppStore = create<AppState>()(
    persist(
        (set, get) => ({
            darkMode: true,
            toggleDarkMode: () => set((state) => ({ darkMode: !state.darkMode })),
            token: undefined,
            refreshToken: undefined,
            tokenExpiry: undefined,
            setToken: (token, expiry) => set({ token, tokenExpiry: expiry }),
            setTokens: (accessToken, refreshToken, expiry) => set({ token: accessToken, refreshToken, tokenExpiry: expiry }),
            userRole: undefined,
            setUserRole: (role) => set({ userRole: role }),
            savedUsername: 'admin',
            setSavedUsername: (username) => set({ savedUsername: username }),
            isTokenExpired: () => {
                const { token, tokenExpiry } = get();
                if (!token || !tokenExpiry) return true;
                // Add 10s buffer
                return (Date.now() / 1000) >= (tokenExpiry - 10);
            },
            clearAuth: () => set({ token: undefined, refreshToken: undefined, tokenExpiry: undefined, userRole: undefined })
        }),
        {
            name: 'app-storage',
        }
    )
);
