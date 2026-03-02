// Hook to manage local email/password authentication
// Stores JWT in localStorage and syncs auth state across the app

export interface LocalAuthState {
    isAuthenticated: boolean;
    accessToken: string | null;
    refreshToken: string | null;
}

export function useLocalAuth() {
    const getAuthState = (): LocalAuthState => {
        const accessToken = localStorage.getItem('accessToken');
        const refreshToken = localStorage.getItem('refreshToken');

        return {
            isAuthenticated: !!accessToken,
            accessToken,
            refreshToken,
        };
    };

    const login = (accessToken: string, refreshToken: string) => {
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        window.dispatchEvent(new Event('localAuthChanged'));
    };

    const logout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.dispatchEvent(new Event('localAuthChanged'));
    };

    const getAccessToken = (): string | null => {
        return localStorage.getItem('accessToken');
    };

    return {
        ...getAuthState(),
        login,
        logout,
        getAccessToken,
        getAuthState,
    };
}
