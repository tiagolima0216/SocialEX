import { useEffect, useState } from "react";
import { API } from "./api";

export function usePushNotifications() {
    const [supported, setSupported] = useState<boolean>(typeof window !== "undefined" && 'serviceWorker' in navigator && 'PushManager' in window);

    useEffect(() => {
        if (!supported) return;
        navigator.serviceWorker.register('/sw.js').then(reg => {
            // registered
        }).catch(console.error);
    }, [supported]);

    async function subscribe(vapidPublicKey: string) {
        const reg = await navigator.serviceWorker.ready;
        const convertedVapidKey = urlBase64ToUint8Array(vapidPublicKey);

        let subscription = await reg.pushManager.getSubscription();
        if (!subscription) {
            subscription = await reg.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: convertedVapidKey
            });
        }

        await API.post("/api/v1/push/subscribe", subscription);
        return subscription;
    }


    function urlBase64ToUint8Array(base64String: string) {
        const padding = "=".repeat((4 - base64String.length % 4) % 4);
        const base64 = (base64String + padding).replace(/\-/g, "+").replace(/_/g, "/");
        const rawData = window.atob(base64);
        const outputArray = new Uint8Array(rawData.length);
        for (let i = 0; i < rawData.length; ++i) {
            outputArray[i] = rawData.charCodeAt(i);
        }
        return outputArray;
    }

    return { supported, subscribe };
}
