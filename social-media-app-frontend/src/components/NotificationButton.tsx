import {useEffect, useState} from "react";
import { API } from "../api/api";
import { useStomp } from "../hooks/useStomp";

export default function NotificationsButton() {
    const [count, setCount] = useState(0);

    useStomp((notif) => {
        if (notif.type === "FRIEND_REQUEST" || notif.type === "MESSAGE") {
            setCount((c) => c + 1);
        }
    });

    useEffect(() => {
        API.get('/api/v1/notifications/unreadCount').then(res => setCount(res.data.count));
    }, []);

    return <button>Requests ({count})</button>;
}
