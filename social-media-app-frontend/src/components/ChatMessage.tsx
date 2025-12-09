// ChatMessage.tsx
import dayjs from "dayjs";
import type { Message } from "../models/Message.ts";

interface ChatMessageProps {
    message: Message;
    isMe: boolean;
    friendImage?: string;
}

export default function ChatMessage({ message, isMe, friendImage }: ChatMessageProps) {
    return (
        <div className={`flex items-end ${isMe ? "justify-end" : "justify-start"}`}>
            {!isMe && friendImage && (
                <img
                    src={friendImage}
                    alt="Friend"
                    className="w-8 h-8 rounded-full mr-2 flex-shrink-0"
                />
            )}
            <div className="max-w-xs break-words">
                <div
                    className={`px-3 py-2 rounded-lg ${
                        isMe ? "bg-blue-500 text-white" : "bg-gray-200 text-gray-900"
                    }`}
                >
                    {message.content}
                </div>
                <div
                    className={`text-xs mt-1 text-gray-500 ${
                        isMe ? "text-right" : "text-left"
                    }`}
                >
                    {dayjs(message.createdAt).format("HH:mm")} {isMe && message.readFlag ? "✓" : ""}
                </div>
            </div>
            {isMe && <div className="w-8 h-8 ml-2 flex-shrink-0"></div>}
        </div>
    );
}
