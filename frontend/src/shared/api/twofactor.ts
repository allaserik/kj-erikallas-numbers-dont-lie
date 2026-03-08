import { api } from "./client";

export type TwoFactorStatusResponse = {
    enabled: boolean;
};

export type TwoFactorSetupResponse = {
    secret: string;
    otpauthUri: string;
};

export function getTwoFactorStatus(token: string): Promise<TwoFactorStatusResponse> {
    return api.get<TwoFactorStatusResponse>("/api/auth/2fa/status", token);
}

export function setupTwoFactor(token: string): Promise<TwoFactorSetupResponse> {
    return api.post<TwoFactorSetupResponse>("/api/auth/2fa/setup", {}, token);
}

export function enableTwoFactor(code: string, token: string): Promise<TwoFactorStatusResponse> {
    return api.post<TwoFactorStatusResponse>("/api/auth/2fa/enable", { code }, token);
}

export function disableTwoFactor(code: string, token: string): Promise<TwoFactorStatusResponse> {
    return api.post<TwoFactorStatusResponse>("/api/auth/2fa/disable", { code }, token);
}
