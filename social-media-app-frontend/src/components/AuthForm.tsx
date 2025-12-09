import axios from "axios";
import { useState } from "react";
import { signup, login } from "../api/api";
import { Eye, EyeOff } from "lucide-react"; // optional, you can use any icon library

interface AuthFormProps {
    onLogin: (token: string, username: string, userId: string) => void;
}


type MessageType = "error" | "success";

export default function AuthForm({ onLogin }: AuthFormProps) {
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [message, setMessage] = useState("");
    const [messageType, setMessageType] = useState<MessageType>("success"); // default
    const [shake, setShake] = useState(false);
    const [showPassword, setShowPassword] = useState(false);

    const handleSignup = async () => {
        try {
            await signup(username, password);
            setMessage("Signup successful! Now log in.");
            setMessageType("success");
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                setMessage(err.response?.data?.message || "Signup failed");
            } else {
                setMessage("Signup failed");
            }
            setMessageType("error");
            setShake(true);
            setTimeout(() => setShake(false), 300);
        }
    };

    const handleLogin = async () => {
        try {
            const res = await login(username, password); // res now has token + id
            setMessage("Login successful!");
            setMessageType("success");

            // Pass token, username, and userId back to App
            onLogin(res.token, username, res.id);
        } catch (err: unknown) {
            if (axios.isAxiosError(err)) {
                setMessage(err.response?.data?.message || "Login failed");
            } else {
                setMessage("Login failed");
            }
            setMessageType("error");
            setShake(true);
            setTimeout(() => setShake(false), 300);
        }
    };



    return (
        <div className="bg-white p-6 rounded-2xl shadow-lg w-80 flex flex-col gap-4">
            <h2 className="text-xl font-bold text-center">Sign In</h2>
            <input
                type="text"
                placeholder="Username"
                className="border p-2 rounded-lg focus:ring-2 focus:ring-blue-300 focus:outline-none transition-all duration-150"
                onChange={(e) => setUsername(e.target.value)}
            />
            <div className="relative">
                <input
                    type={showPassword ? "text" : "password"}
                    placeholder="Password"
                    className="w-full border p-2 rounded-lg focus:ring-2 focus:ring-blue-300 focus:outline-none transition-all duration-150"
                    onChange={(e) => setPassword(e.target.value)}
                />
                <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-gray-500 hover:text-gray-700"
                >
                    {showPassword ? <EyeOff size={18}/> : <Eye size={18}/>}
                </button>
            </div>

            <button
                onClick={handleSignup}
                className=" bg-blue-500 text-white font-medium py-2 rounded-lg transition
                            hover:bg-blue-600 active:bg-blue-700
                            active:scale-95 active:shadow-lg">
                Signup
            </button>
            <button
                onClick={handleLogin}
                className=" bg-green-500 text-white font-medium py-2 rounded-lg transition
                            hover:bg-green-600 active:bg-green-700
                            active:scale-95 active:shadow-lg">
                Login
            </button>
            {message && (
                <div className="flex flex-col gap-2">
                    {messageType === "error"
                        ? message.split(";").map((err, idx) => (
                            <div
                                key={idx}
                                className={`flex items-center gap-2 bg-red-50 border border-red-400 text-red-700 p-2 rounded-md text-sm
                    ${shake ? "animate-shake" : ""}`}
                            >
                                <span className="font-bold">!</span>
                                <span>{err.trim()}</span>
                            </div>
                        ))
                        : (
                            <div
                                className="bg-green-50 border border-green-400 text-green-700 p-2 rounded-md text-sm text-center font-medium">
                                {message}
                            </div>
                        )
                    }
                </div>
            )}
        </div>
    );
}
