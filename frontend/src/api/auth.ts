/**
 * Authentication API calls for email/password auth
 */

export interface AuthLoginRequest {
    email: string;
    password: string;
    twoFactorCode?: string;
}

export interface AuthRegisterRequest {
    email: string;
    password: string;
}

// Login response as returned by backend (snake_case JSON)
interface LoginResponseJson {
    access_token: string;
    refresh_token: string;
    token_type?: string;
    expires_in?: number;
}

// Normalized login response (camelCase)
export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
}

export interface RegisterResponse {
    id: string;
    email: string;
    emailVerified: boolean;
    message?: string;
}

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080';

/**
 * Register a new user with email and password
 */
export async function registerUser(email: string, password: string): Promise<RegisterResponse> {
    const response = await fetch(`${API_BASE}/api/auth/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password } as AuthLoginRequest),
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Registration failed');
    }

    return response.json();
}

/**
 * Login with email and password
 * Returns JWT access token and refresh token
 */
export async function loginUser(email: string, password: string, twoFactorCode?: string): Promise<LoginResponse> {
    const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password, twoFactorCode } as AuthLoginRequest),
    });

    if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Login failed');
    }

    const data: LoginResponseJson = await response.json();
    return {
        accessToken: data.access_token,
        refreshToken: data.refresh_token,
    };
}
