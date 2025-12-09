import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useEffect, useRef, useState } from "react";
import { API } from "../api/api";
import type { Message } from "../models/Message";
import type { Conversation } from "../models/Conversation";
import type { UserDto } from "../models/UserDto";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";

dayjs.extend(utc);

export function useMessages() {
    const [conversations, setConversations] = useState<Conversation[]>([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const clientRef = useRef<Client | null>(null);

    const sortMessages = (messages: Message[]) =>
        [...messages].sort((a, b) => dayjs.utc(a.createdAt).valueOf() - dayjs.utc(b.createdAt).valueOf());

    const mergeMessages = (oldMsgs: Message[], newMsg: Message) => {
        const withoutDup = oldMsgs.filter((m) => m.id !== newMsg.id);
        return [...withoutDup, newMsg].sort(
            (a, b) => dayjs.utc(a.createdAt).valueOf() - dayjs.utc(b.createdAt).valueOf()
        );
    };

    const enrichMessage = (m: Message, conv: Conversation): Message => {
        if (m.sender) return m;
        const sender = conv.participants.find((p: UserDto) => String(p.id) === String(m.senderId)) ?? null;
        return { ...m, sender };
    };

    useEffect(() => {
        let mounted = true;

        const fetchConversations = async () => {
            try {
                const res = await API.get<Conversation[]>("/api/v1/messages/conversations");
                if (!mounted) return;

                const convs = res.data.map((c) => ({
                    ...c,
                    messages: sortMessages(c.messages.map((m) => ({
                        ...enrichMessage(m, c),
                        createdAt: dayjs.utc(m.createdAt).toISOString()
                    }))),
                }));

                setConversations(convs);

                const unread = convs.reduce(
                    (sum, c) => sum + c.messages.filter((m) => !m.readFlag).length,
                    0
                );
                setUnreadCount(unread);
            } catch (err) {
                console.error("Failed to load conversations", err);
            }
        };

        fetchConversations();

        const token = localStorage.getItem("token");
        const socket = new SockJS(`${import.meta.env.VITE_API_URL}/ws?token=${token}`);

        const client = new Client({
            webSocketFactory: () => socket as any,
            debug: (str) => console.log("STOMP DEBUG:", str),
            onConnect: () => {
                console.log("✅ STOMP connected");
                client.subscribe("/user/queue/messages", (msg) => {
                    if (msg.body) {
                        const incoming: Message = JSON.parse(msg.body);

                        setConversations((prev) =>
                            prev.map((conv) => {
                                if (conv.id !== incoming.conversationId) return conv;

                                const enriched = {
                                    ...enrichMessage(incoming, conv),
                                    createdAt: dayjs.utc(incoming.createdAt).toISOString()
                                };

                                const merged = mergeMessages(conv.messages, enriched);

                                return {
                                    ...conv,
                                    messages: merged,
                                };
                            })
                        );
                    }
                });
            },
        });

        client.activate();
        clientRef.current = client;

        return () => {
            mounted = false;
            clientRef.current?.deactivate();
        };
    }, []);

    return { conversations, setConversations, unreadCount };
}
