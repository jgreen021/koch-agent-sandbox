import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AppState {
    darkMode: boolean;
    toggleDarkMode: () => void;
    token: string | undefined;
    tokenExpiry: number | undefined;
    setToken: (token: string | undefined, expiry?: number) => void;
    userRole: string | undefined;
    setUserRole: (role: string | undefined) => void;
    savedUsername: string;
    setSavedUsername: (username: string) => void;
    isTokenExpired: () => boolean;
}

export const useAppStore = create<AppState>()(
    persist(
        (set, get) => ({
            darkMode: true,
            toggleDarkMode: () => set((state) => ({ darkMode: !state.darkMode })),
            token: undefined,
            tokenExpiry: undefined,
            setToken: (token, expiry) => set({ token, tokenExpiry: expiry }),
            userRole: undefined,
            setUserRole: (role) => set({ userRole: role }),
            savedUsername: 'admin',
            setSavedUsername: (username) => set({ savedUsername: username }),
            isTokenExpired: () => {
                const { token, tokenExpiry } = get();
                if (!token || !tokenExpiry) return true;
                return Date.now() >= tokenExpiry * 1000;
            }
        }),
        {
            name: 'app-storage',
        }
    )
);
