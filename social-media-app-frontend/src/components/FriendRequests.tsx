// components/FriendRequests.tsx
import { useEffect, useState } from "react";
import { API } from "../api/api";

type FriendRequest = {
    id: string;
    requester: {
        id: string;
        username: string;
        profilePicture?: string | null;
    };
    createdAt: string;
};

export default function FriendRequests() {
    const [requests, setRequests] = useState<FriendRequest[]>([]);
    const [loading, setLoading] = useState(true);
    const [profilePictures, setProfilePictures] = useState<Record<string, string>>({});

    async function fetchRequests() {
        setLoading(true);
        try {
            const res = await API.get<FriendRequest[]>("/api/v1/friends/requests");
            setRequests(res.data);

            // fetch profile pics as blobs
            const pics: Record<string, string> = {};
            await Promise.all(
                res.data.map(async (req) => {
                    if (req.requester.profilePicture) {
                        try {
                            const imgRes = await API.get(
                                `/api/v1/users/${req.requester.id}/profile-picture`,
                                { responseType: "blob" }
                            );
                            pics[req.requester.id] = URL.createObjectURL(imgRes.data);
                        } catch (err) {
                            console.error("Failed to load picture for", req.requester.username, err);
                        }
                    }
                })
            );
            setProfilePictures(pics);
        } finally {
            setLoading(false);
        }
    }

    async function respond(requestId: string, accept: boolean) {
        await API.post(`/api/v1/friends/requests/${requestId}/respond?accept=${accept}`);
        await fetchRequests(); // refresh list
    }

    useEffect(() => {
        fetchRequests();

        // cleanup blob URLs on unmount
        return () => {
            Object.values(profilePictures).forEach((url) => URL.revokeObjectURL(url));
        };
    }, []);

    if (loading) return <p>Loading friend requests...</p>;
    if (requests.length === 0) return <p>No pending friend requests</p>;

    return (
        <div className="p-4 border rounded-lg bg-white shadow-md">
            <h2 className="text-lg font-bold mb-2">Friend Requests</h2>
            <ul className="space-y-3">
                {requests.map((req) => (
                    <li
                        key={req.id}
                        className="flex items-center justify-between border-b pb-2 last:border-b-0"
                    >
                        <div className="flex items-center gap-3">
                            <img
                                src={
                                    profilePictures[req.requester.id] ||
                                    "/default_profile_SocialEX.png"
                                }
                                alt={req.requester.username}
                                className="w-10 h-10 rounded-full border"
                            />
                            <span className="font-medium">{req.requester.username}</span>
                        </div>
                        <div className="flex gap-2">
                            <button
                                onClick={() => respond(req.id, true)}
                                className="px-3 py-1 rounded bg-green-500 text-white hover:bg-green-600"
                            >
                                Accept
                            </button>
                            <button
                                onClick={() => respond(req.id, false)}
                                className="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600"
                            >
                                Reject
                            </button>
                        </div>
                    </li>
                ))}
            </ul>
        </div>
    );
}
