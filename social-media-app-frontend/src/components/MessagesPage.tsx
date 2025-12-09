import { useState, useEffect, useRef, useMemo } from "react";
import { useMessages } from "../hooks/useMessages";
import { API } from "../api/api";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import type { Message } from "../models/Message.ts";
import type { UserDto } from "../models/UserDto.ts";

dayjs.extend(utc);

export default function MessagesPage() {
    const { conversations, setConversations } = useMessages();
    const [activeConvId, setActiveConvId] = useState<string | null>(null);
    const [newMessage, setNewMessage] = useState("");
    const scrollRef = useRef<HTMLDivElement | null>(null);
    const textareaRef = useRef<HTMLTextAreaElement | null>(null);
    const activeConv = conversations.find((c) => c.id === activeConvId);
    const API_BASE_URL = import.meta.env.VITE_API_URL ?? "";
    const currentUserId = localStorage.getItem("userId");
    const currentUsername = localStorage.getItem("currentUsername");

    const resolveUrl = (path?: string | null) => {
        if (!path) return "/default_profile_SocialEX.png";
        if (path.startsWith("http")) return path;
        return `${API_BASE_URL}${path}`;
    };

    const adjustTextareaHeight = () => {
        const ta = textareaRef.current;
        if (!ta) return;
        ta.style.height = "auto";
        ta.style.height = `${Math.min(ta.scrollHeight, 200)}px`;
    };

    useEffect(() => {
        adjustTextareaHeight();
    }, [newMessage]);

    const scrollToBottom = () => {
        if (!scrollRef.current) return;
        setTimeout(() => {
            scrollRef.current!.scrollTop = scrollRef.current!.scrollHeight;
        }, 40);
    };

    const buildMySenderFromConv = (conv?: any): UserDto & { id: string; username: string } => {
        const me = conv?.participants?.find((p: any) => String(p.id) === String(currentUserId));
        return {
            id: me?.id ?? currentUserId ?? "unknown-id",
            username: me?.username ?? currentUsername ?? "You",
            profilePicture: me?.profilePicture ?? null,
            bio: me?.bio,
            createdAt: me?.signupDate ?? me?.createdAt,
        };
    };

    const ts = (iso?: string) => (iso ? dayjs.utc(iso).valueOf() : 0);

    const isMessageFromMe = (m: Message): boolean => {
        const senderId = m.sender?.id ?? m.senderId;
        const senderName = m.sender?.username ?? m.senderUsername ?? "";
        return senderId
            ? String(senderId).trim() === String(currentUserId)?.trim()
            : senderName.toLowerCase() === currentUsername?.toLowerCase();
    };

    const mergeMessages = (oldMsgs: Message[], newMsg: Message) => {
        const withoutDup = oldMsgs.filter((m) => m.id !== newMsg.id);
        return [...withoutDup, newMsg].sort(
            (a, b) => dayjs.utc(a.createdAt).valueOf() - dayjs.utc(b.createdAt).valueOf()
        );
    };

    const sendMessage = async () => {
        if (!activeConvId || !newMessage.trim()) return;

        const mySender = buildMySenderFromConv(activeConv);
        const tempId = `temp-${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;

        const optimistic: Message = {
            id: tempId,
            senderId: currentUserId!,
            senderUsername: currentUsername!,
            sender: mySender,
            content: newMessage,
            createdAt: dayjs.utc().toISOString(),
            readFlag: false,
            conversationId: activeConvId!,
        };

        setConversations((prev) =>
            prev.map((conv) =>
                conv.id === activeConvId
                    ? { ...conv, messages: mergeMessages(conv.messages, optimistic) }
                    : conv
            )
        );

        setNewMessage("");
        adjustTextareaHeight();
        scrollToBottom();

        try {
            const resp = await API.post(`/api/v1/messages/${activeConvId}`, {
                content: optimistic.content,
            });
            const created: Message = {
                ...resp.data,
                createdAt: dayjs.utc(resp.data.createdAt).toISOString(),
                sender: resp.data.sender ?? mySender,
            };

            setConversations((prev) =>
                prev.map((conv) =>
                    conv.id === activeConvId
                        ? {
                            ...conv,
                            messages: mergeMessages(
                                conv.messages.filter((m) => m.id !== tempId),
                                created
                            ),
                        }
                        : conv
                )
            );
        } catch (err) {
            console.error("Send failed", err);
        }
    };

    useEffect(() => {
        if (!activeConvId) return;
        (async () => {
            try {
                await API.put(`/api/v1/messages/${activeConvId}/read`);
                setConversations((prev) =>
                    prev.map((conv) =>
                        conv.id === activeConvId
                            ? {
                                ...conv,
                                messages: conv.messages.map((m) =>
                                    (m.sender?.id ?? m.senderId) !== currentUserId ? { ...m, readFlag: true } : m
                                ),
                            }
                            : conv
                    )
                );
            } catch (e) {
                console.error("Failed to mark read", e);
            }
        })();
    }, [activeConvId]);

    useEffect(() => {
        scrollToBottom();
    }, [activeConv?.messages]);

    const sortedMessages = useMemo(() => {
        return [...(activeConv?.messages || [])].sort(
            (a, b) => ts(a.createdAt) - ts(b.createdAt)
        );
    }, [activeConv?.messages]);

    const sortedConversations = useMemo(() => {
        return [...conversations].sort((a, b) => {
            const aLast = a.messages.length
                ? Math.max(...a.messages.map((m) => ts(m.createdAt)))
                : 0;
            const bLast = b.messages.length
                ? Math.max(...b.messages.map((m) => ts(m.createdAt)))
                : 0;
            return bLast - aLast;
        });
    }, [conversations]);

    return (
        <div className="flex flex-col h-screen">
            <div className="flex h-screen pb-12 w-full overflow-hidden">
                {/* Sidebar */}
                <div className="w-1/3 border-r overflow-y-auto bg-white min-w-[250px]">
                    {sortedConversations.map((c) => {
                        const sortedMsgs = [...(c.messages || [])].sort(
                            (a, b) => ts(a.createdAt) - ts(b.createdAt)
                        );
                        const lastMsg = sortedMsgs[sortedMsgs.length - 1];
                        const lastSenderId = lastMsg?.sender?.id ?? lastMsg?.senderId;

                        const otherParticipants = (c.participants || []).filter(
                            (p: any) => String(p.id) !== String(currentUserId)
                        );

                        const participantLastTime = (p: any) => {
                            const msg = [...(c.messages || [])].reverse().find(
                                (m: any) => (m.sender?.id ?? m.senderId) === p.id
                            );
                            return msg ? ts(msg.createdAt) : 0;
                        };

                        const otherParticipantsSorted = otherParticipants.sort(
                            (p1: any, p2: any) => participantLastTime(p2) - participantLastTime(p1)
                        );

                        return (
                            <div
                                key={c.id}
                                className={`p-3 cursor-pointer flex items-center ${c.id === activeConvId ? "bg-gray-100" : "hover:bg-gray-50"}`}
                                onClick={() => setActiveConvId(c.id)}
                            >
                                <div className="flex -space-x-2 mr-3">
                                    {otherParticipantsSorted.map((p: any) => {
                                        const isLastSender = lastSenderId && String(lastSenderId) === String(p.id);
                                        return (
                                            <img
                                                key={p.id}
                                                src={p.profilePicture ? resolveUrl(p.profilePicture) : "/default_profile_SocialEX.png"}
                                                alt={p.username}
                                                className={`w-8 h-8 rounded-full border-2 ${isLastSender ? "border-blue-500 ring-2 ring-blue-400" : "border-gray-200"}`}
                                            />
                                        );
                                    })}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <div className="font-medium truncate">
                                        {otherParticipantsSorted.map((p: any) => p.username).join(", ")}
                                    </div>
                                    <div className="text-sm text-gray-500 flex flex-col">
                                        <span className="truncate">
                                            {lastMsg
                                                ? `${isMessageFromMe(lastMsg) ? "You" : lastMsg.sender?.username}: ${lastMsg.content}`
                                                : "No messages yet"}
                                        </span>
                                        <span className="text-xs text-gray-400 mt-1">
                                            {lastMsg ? dayjs.utc(lastMsg.createdAt).format("HH:mm · DD/MM/YYYY") : ""}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        );
                    })}
                </div>

                {/* Chat area */}
                <div className="flex-1 flex flex-col relative bg-gray-50 min-w-[500px] max-w-full">
                    {activeConv ? (
                        <>
                            <div ref={scrollRef} className="flex-1 overflow-y-auto p-4 space-y-4 pb-32">
                                {sortedMessages.map((m: Message) => {
                                    const isMe = isMessageFromMe(m);
                                    const profilePic = m.sender?.profilePicture ? resolveUrl(m.sender.profilePicture) : "/default_profile_SocialEX.png";
                                    const senderUsername = m.sender?.username ?? "Unknown";

                                    return (
                                        <div key={m.id} className={`flex items-end ${isMe ? "justify-end" : "justify-start"}`}>
                                            {!isMe && <img src={profilePic} alt={senderUsername} className="w-8 h-8 rounded-full mr-2 flex-shrink-0" />}
                                            <div className={`max-w-[65%] sm:max-w-[60%] p-3 rounded-lg shadow break-words break-all whitespace-pre-wrap ${isMe ? "bg-blue-500 text-white" : "bg-white text-gray-900"}`}>
                                                {!isMe && <div className="text-xs font-semibold text-gray-600 mb-1">{senderUsername}</div>}
                                                <div className="text-sm">{m.content}</div>
                                                <div className={`text-xs mt-1 ${isMe ? "text-right text-blue-100" : "text-left text-gray-500"}`}>
                                                    {dayjs.utc(m.createdAt).format("HH:mm · DD MMM YYYY")}
                                                </div>
                                            </div>
                                            {isMe && <img src={profilePic} alt="Me" className="w-8 h-8 rounded-full ml-2 flex-shrink-0" />}
                                        </div>
                                    );
                                })}
                            </div>

                            <div className="p-3 border-t bg-white flex items-end">
                                <textarea
                                    ref={textareaRef}
                                    rows={1}
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onInput={adjustTextareaHeight}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter" && !e.shiftKey) {
                                            e.preventDefault();
                                            sendMessage();
                                        }
                                    }}
                                    placeholder="Type a message... (Shift+Enter for newline)"
                                    className="flex-1 resize-none border rounded-xl px-3 py-2 mr-2 focus:outline-none focus:ring focus:ring-blue-200 h-auto max-h-52 placeholder:text-gray-400 placeholder:whitespace-nowrap placeholder:overflow-hidden"
                                />
                                <button onClick={sendMessage} className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-full">
                                    Send
                                </button>
                            </div>
                        </>
                    ) : (
                        <div className="flex-1 flex items-center justify-center text-gray-400">Select a conversation</div>
                    )}
                </div>
            </div>
        </div>
    );
}
