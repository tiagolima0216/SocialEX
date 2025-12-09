import { Client, type IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { useEffect, useRef } from 'react';

export function useStomp(onNotification: (msg: Record<string, unknown>) => void) {
    const clientRef = useRef<Client | null>(null);

    useEffect(() => {
        const client = new Client({
            webSocketFactory: () => new SockJS('/ws'),
            debug: (str) => console.log(str),
            onConnect: () => {
                client.subscribe('/user/queue/notifications', (msg: IMessage) => {
                    try {
                        const body = JSON.parse(msg.body) as Record<string, unknown>;
                        onNotification(body);
                    } catch (err) {
                        console.error("Failed to parse notification:", err);
                    }
                });
            },
            onStompError: (frame) => console.error(frame),
        });

        client.activate();
        clientRef.current = client;

        return () => {
            // 👇 use optional chaining so no "possibly null" warning
            clientRef.current?.deactivate();
        };
    }, [onNotification]);

    return clientRef;
}
