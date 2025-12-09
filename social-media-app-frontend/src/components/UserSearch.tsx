import React, { useEffect, useMemo, useState } from "react";
import { searchUsers } from "../api/api";
import { useNavigate } from "react-router-dom";

type UserHit = {
    id: string;
    username: string;
    displayName: string;
    profilePicture?: string | null;
};

export default function UserSearch() {
    const [q, setQ] = useState("");
    const [results, setResults] = useState<UserHit[]>([]);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const debouncedQ = useMemo(() => q, [q]);
    const API_BASE_URL = import.meta.env.VITE_API_URL;

    useEffect(() => {
        const t = setTimeout(async () => {
            if (!debouncedQ.trim()) { setResults([]); return; }
            setLoading(true);
            try {
                const hits = await searchUsers(debouncedQ);
                setResults(hits);

            } finally {
                setLoading(false);
            }
        }, 250);
        return () => clearTimeout(t);
    }, [debouncedQ]);

    return (
        <div className="w-full max-w-xl">
            <input
                value={q}
                onChange={(e) => setQ(e.target.value)}
                placeholder="Search users"
                className="w-full border rounded-lg px-3 py-2"
            />
            {loading && <div className="text-sm text-gray-500 mt-2">Searching…</div>}

            {results.length > 0 && (
                <div className="mt-2 bg-white border rounded-lg shadow divide-y">
                    {results.map(u => (
                        <button
                            key={u.id}
                            onClick={() => navigate(`/profile/${u.id}`)}
                            className="w-full text-left flex items-center gap-3 p-2 hover:bg-gray-50"
                        >
                            <img
                                src={u.profilePicture ? `${API_BASE_URL}${u.profilePicture}` : "/default_profile_SocialEX.png"}
                                alt={u.username}
                                className="w-10 h-10 rounded-full border"
                            />
                            <div className="flex flex-col">
                                <span className="font-medium">{u.displayName || u.username}</span>
                                <span className="text-sm text-gray-500">@{u.username}</span>
                            </div>
                        </button>
                    ))}
                </div>
            )}
        </div>
    );
}
