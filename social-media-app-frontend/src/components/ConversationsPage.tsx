import { Link } from "react-router-dom";
import dayjs from "dayjs";
import { useMessages } from "../hooks/useMessages.ts";
import useProfileImage from "../hooks/useProfileImage.ts";
import { useEffect, useState } from "react";

export default function ConversationsPage() {
    const { conversations } = useMessages();
    const [images, setImages] = useState<Record<string, string>>({});

    // Load images for all friends at once
    useEffect(() => {
        conversations.forEach((c) => {
            if (!c.friendId) return;
            // Only fetch if we don't already have it
            if (!images[c.friendId]) {
                useProfileImage(c.friendId)
                    .then((img) => setImages((prev) => ({ ...prev, [c.friendId]: img })))
                    .catch(() => {
                        // fallback to default image
                        setImages((prev) => ({ ...prev, [c.friendId]: "/default_profile_SocialEX.png" }));
                    });
            }
        });
    }, [conversations]);

    return (
        <div className="flex flex-col h-screen p-4">
            <h1 className="text-xl font-bold mb-4">Conversations</h1>
            <ul className="flex-1 overflow-y-auto">
                {conversations.map((c) => {
                    const lastMsg = c.messages[c.messages.length - 1];
                    const lastMsgTime = lastMsg ? dayjs(lastMsg.createdAt).format("HH:mm") : "";
                    const isSeen = lastMsg?.readFlag && lastMsg.senderId !== c.friendId;

                    return (
                        <li key={c.id} className="mb-2">
                            <Link
                                to={`/messages/${c.friendId}`}
                                className="flex items-center p-2 rounded hover:bg-gray-100"
                            >
                                <img
                                    src={images[c.friendId] || "/default_profile_SocialEX.png"}
                                    alt={c.friendUsername}
                                    className="w-12 h-12 rounded-full mr-3"
                                />
                                <div className="flex-1">
                                    <div className="font-medium">{c.friendUsername}</div>
                                    <div className="text-sm text-gray-500 flex justify-between">
                                        <span>{lastMsg?.content || "No messages yet"}</span>
                                        <span className="ml-2 text-xs">{lastMsgTime} {isSeen ? "✓" : ""}</span>
                                    </div>
                                </div>
                            </Link>
                        </li>
                    );
                })}
            </ul>
        </div>
    );
}
