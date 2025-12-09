import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {fetchUser, API, logout} from "../api/api"; // ✅ import API to use JWT
import type { UserDto } from "../models/UserDto";

interface ProfilePageProps {
    onLogout: () => void;
}

export default function ProfilePage() {
    const { userId } = useParams<{ userId: string }>();
    const [user, setUser] = useState<UserDto | null>(null);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    const loggedInUserId = localStorage.getItem("userId"); // from login

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

    const handleLogoutClick = async () => {
        await logout();
        localStorage.clear();
        window.location.href = "/"; // reload app
    };

    const isMine = loggedInUserId === userId;

    async function sendFriendRequest() {
        try {
            await API.post(`/api/v1/friends/${userId}/request`);
            alert("Friend request sent!");
        } catch (err) {
            console.error(err);
            alert("Failed to send request");
        }
    }

    async function startConversation() {
        try {
            const res = await API.post(`/api/v1/messages/start/${userId}`);
            const conversationId = res.data.id;
            window.location.href = `/messages?conv=${conversationId}`;
        } catch (err) {
            console.error(err);
            alert("Failed to start conversation");
        }
    }


    if (loading) return <p>Loading...</p>;
    if (!user) return <p>User not found</p>;

    const API_BASE_URL = import.meta.env.VITE_API_URL;

    return (
        <div className="flex flex-col items-center gap-4 pb-12">
            <img
                src={user.profilePicture ? `${API_BASE_URL}${user.profilePicture}` : "/default_profile_SocialEX.png"}
                alt={`${user.username} profile`}
                className="w-32 h-32 rounded-full border-2 border-gray-400"
                loading={"lazy"}
            />
            <h2 className="text-xl font-bold">{user.username}</h2>
            <p>{user.bio}</p>

            {loggedInUserId === userId && (
                <>
                    <button
                        onClick={() => navigate(`/profile/${userId}/edit`)}
                        className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg"
                    >
                        Edit Profile
                    </button>
                    <button
                        onClick={handleLogoutClick}
                        className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-lg mt-2"
                    >
                        Logout
                    </button>
                </>
            )}

            {!isMine && (
                <>
                    <button
                        onClick={sendFriendRequest}
                        className="bg-green-500 hover:bg-green-600 text-white px-4 py-2 rounded-lg"
                    >
                        Add Friend
                    </button>
                    <button
                        onClick={startConversation}
                        className="ml-2 bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-lg"
                    >
                        Start Conversation
                    </button>
                </>
            )}

        </div>
    );
}
