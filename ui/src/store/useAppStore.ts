import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AppState {
    darkMode: boolean;
    toggleDarkMode: () => void;
    token: string | undefined;
    setToken: (token: string | undefined) => void;
    savedUsername: string;
    setSavedUsername: (username: string) => void;
}

export const useAppStore = create<AppState>()(
    persist(
        (set) => ({
            darkMode: true,
            toggleDarkMode: () => set((state) => ({ darkMode: !state.darkMode })),
            token: undefined, // Cleared mock JWT Token
            setToken: (token) => set({ token }),
            savedUsername: 'admin',
            setSavedUsername: (username) => set({ savedUsername: username }),
        }),
        {
            name: 'app-storage',
        }
    )
);
