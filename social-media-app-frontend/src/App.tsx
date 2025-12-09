import { useEffect, useState } from "react";
import AuthForm from "./components/AuthForm";
import MainPage from "./components/MainPage";
import {logout, refreshToken} from "./api/api";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProfilePage from "./components/ProfilePage";
import EditProfilePage from "./components/EditProfilePage";
import BottomMenu from "./components/BottomMenu.tsx";
import MessagesPage from "./components/MessagesPage.tsx";


function App() {
    const [loading, setLoading] = useState(true);
    const [token, setToken] = useState<string | null>(null);
    const [username, setUsername] = useState<string | null>(null);
    const [userId, setUserId] = useState<string | null>(null); // ✅ new state

    // Restore token, username, and userId from localStorage
    useEffect(() => {
        const storedToken = localStorage.getItem("token");
        const storedUsername = localStorage.getItem("username");
        const storedUserId = localStorage.getItem("userId"); // ✅ restore ID
        if (storedToken) {
            setToken(storedToken);
            setUsername(storedUsername);
            setUserId(storedUserId);
        }
    }, []);

    // Try to refresh token on mount
    useEffect(() => {
        const tryRefresh = async () => {
            try {
                const { token, id } = await refreshToken(); // ✅ get ID from backend
                setToken(token);
                setUserId(id); // ✅ save ID in state
                localStorage.setItem("userId", id); // ✅ persist ID
            } catch (err) {
                setToken(null);
                setUserId(null);
            } finally {
                setLoading(false);
            }
        };
        tryRefresh();
    }, []);

    // Auto-refresh token every 30 minutes
    useEffect(() => {
        if (!token) return;

        const interval = setInterval(async () => {
            try {
                const { token, id } = await refreshToken(); // ✅ refresh token + get ID
                setToken(token);
                setUserId(id);
                localStorage.setItem("userId", id);
            } catch (err) {
                await handleLogout(); // token refresh failed, force logout
            }
        }, 30 * 60 * 1000);

        return () => clearInterval(interval);
    }, [token]);

    // Login handler
    const handleLogin = (token: string, username: string, id: string) => {
        localStorage.setItem("token", token);
        localStorage.setItem("username", username);
        localStorage.setItem("userId", id);
        setToken(token);
        setUsername(username);
        setUserId(id);
    };


    // Logout handler
    const handleLogout = async () => {
        await logout();
        setToken(null);
        setUsername(null);
        setUserId(null); // ✅ clear ID
        localStorage.removeItem("userId");
    };


    if (loading) {
        return (
            <div>
                <p>Checking session...</p>
            </div>
        );
    }

    return (
        <BrowserRouter>
            <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
                {!token ? (
                    <>
                        <img
                            src="/word_SocialEX.png"
                            alt="App logo"
                            className="w-64 h-16 mb-6"
                        />
                        <AuthForm onLogin={handleLogin} />
                    </>
                ) : (
                    <>
                        <Routes>
                            <Route
                                path="/"
                                element={<MainPage username={username!} />}
                            />
                            <Route path="/profile/:userId" element={<ProfilePage />} />
                            <Route path="/profile/:userId/edit" element={<EditProfilePage />} />
                            <Route path="/messages" element={<MessagesPage />} />
                            {/* fallback: if route not found, go back home */}
                            <Route path="*" element={<Navigate to="/" />} />
                        </Routes>
                        {userId && <BottomMenu userId={userId} />}
                    </>
                )}
            </div>
        </BrowserRouter>
    );
}

export default App;
