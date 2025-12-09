export interface LoginResponse {
    token: string;
    expiresIn: number;
    cookieUpdated: boolean;
    userId: string; // the new field from backend
}
