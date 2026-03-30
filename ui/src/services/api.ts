import axios from 'axios';
import { useAppStore } from '../store/useAppStore';

const api = axios.create({
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request Interceptor: Attach access token
api.interceptors.request.use((config) => {
    const token = useAppStore.getState().token;
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, (error) => Promise.reject(error));

// Response Interceptor: Handle 401s and Refresh
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function onRefreshed(token: string) {
    refreshSubscribers.map(cb => cb(token));
    refreshSubscribers = [];
}

function subscribeTokenRefresh(cb: (token: string) => void) {
    refreshSubscribers.push(cb);
}

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const { config, response } = error;
        const originalRequest = config;

        if (response?.status === 401 && !originalRequest._retry) {
            if (isRefreshing) {
                return new Promise((resolve) => {
                    subscribeTokenRefresh((token) => {
                        originalRequest.headers.Authorization = `Bearer ${token}`;
                        resolve(api(originalRequest));
                    });
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = useAppStore.getState().refreshToken;
            if (!refreshToken) {
                isRefreshing = false;
                useAppStore.getState().clearAuth();
                window.location.href = '/login';
                return Promise.reject(error);
            }

            try {
                // Call refresh endpoint with the refresh token
                const res = await axios.post('/api/auth/refresh', {}, {
                    headers: { Authorization: `Bearer ${refreshToken}` }
                });

                if (res.status === 200 && res.data.accessToken) {
                    const { accessToken, refreshToken: newRefreshToken, expiresIn } = res.data;
                    
                    // Update store
                    const expiry = Math.floor(Date.now() / 1000) + expiresIn;
                    useAppStore.getState().setTokens(accessToken, newRefreshToken, expiry);
                    
                    onRefreshed(accessToken);
                    isRefreshing = false;

                    originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                    return api(originalRequest);
                }
            } catch (refreshError) {
                isRefreshing = false;
                useAppStore.getState().clearAuth();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }

        return Promise.reject(error);
    }
);

export default api;
