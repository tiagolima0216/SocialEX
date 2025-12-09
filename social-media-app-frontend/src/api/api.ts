import axios from "axios";
import type { UserDto } from "../models/UserDto";

export const API = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    withCredentials: true,
});

API.interceptors.request.use(config => {
    const token = localStorage.getItem("token");
    if (token) {
        config.headers = config.headers || {};
        config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
});

// Auth
export const signup = async (username: string, password: string) =>
    API.post("api/v1/auth/signup", { username, password });

export const login = async (username: string, password: string): Promise<{ token: string; id: string }> => {
    const res = await API.post<{ token: string; id: string }>(
        "api/v1/auth/login",
        { username, password }
    );
    const { token, id } = res.data;
    localStorage.setItem("token", token);
    localStorage.setItem("username", username);
    localStorage.setItem("userId", id);
    return { token, id };
};

export const refreshToken = async (): Promise<{ token: string; id: string }> => {
    const res = await API.post<{ token: string; id: string }>("api/v1/auth/refresh");
    const { token, id } = res.data;
    localStorage.setItem("token", token);
    localStorage.setItem("userId", id);
    return { token, id };
};

// Logout
export const logout = async () => {
    await API.post("api/v1/auth/logout");
    localStorage.removeItem("token");
    localStorage.removeItem("username");
    localStorage.removeItem("userId")
};


export const fetchUser = async (userId: string): Promise<UserDto> => {
    const res = await API.get<UserDto>(`/api/v1/users/${userId}`);
    return res.data;
};

export async function searchUsers(q: string) {
    if (!q.trim()) return [];
    const res = await API.get(`/api/v1/search/users`, { params: { q, size: 8 }});
    return res.data as Array<{
        id: string;
        username: string;
        displayName: string;
        profilePicture?: string | null;
    }>;
}
