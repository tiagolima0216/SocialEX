import type {Message} from "./Message.ts";
import type {UserDto} from "./UserDto.ts";

export interface Conversation {
    id: string;
    title?: string | null;
    participants: UserDto[];
    messages: Message[];
}