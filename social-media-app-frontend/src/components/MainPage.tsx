import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import UserSearch from "./UserSearch";
import FriendRequests from "../components/FriendRequests";
import type {UserDto} from "../models/UserDto.ts";
import {fetchUser} from "../api/api.ts";

interface MainPageProps {
    username: string;
}

export default function MainPage({ username }: MainPageProps) {
    const navigate = useNavigate();
    const userId = localStorage.getItem("userId");
    const [user, setUser] = useState<UserDto | null>(null);
    const API_BASE_URL = import.meta.env.VITE_API_URL;
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!userId) return;

        const loadUser = async () => {
            try {
                // 1️⃣ Fetch user info
                const res = await fetchUser(userId);
                setUser(res);

            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        loadUser();
    }, [userId]);


    if (loading) return <p>Loading...</p>;
    if (!user) return <p>User not found</p>;

    return (
        <div className="flex flex-col items-center gap-4 w-full max-w-xl pb-12">
            <div className="flex justify-start items-center w-full gap-4">
                <img
                    src={user.profilePicture ? `${API_BASE_URL}${user.profilePicture}` : "/default_profile_SocialEX.png"}
                    alt="Profile"
                    className="w-12 h-12 rounded-full border-2 border-gray-400 cursor-pointer"
                    onClick={() => userId && navigate(`/profile/${userId}`)}
                />
                <h1 className="text-2xl font-bold">{username}</h1>
            </div>

            <UserSearch/>

            <div className="space-y-6">
                <FriendRequests/>
            </div>
        </div>
    );

}
