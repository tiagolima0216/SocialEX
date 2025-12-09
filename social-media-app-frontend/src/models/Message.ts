import type {UserDto} from "./UserDto.ts";

export interface Message {
    id: string;
    senderId?: string;
    senderUsername?: string;
    sender?: UserDto; // prefer this when backend sends full sender
    content: string;
    createdAt: string; // ISO string
    timestamp?: string;
    readFlag: boolean;
    conversationId?: string;
}