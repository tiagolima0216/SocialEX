import type { UserDto } from "./UserDto";

export interface Post {
    id: string;
    content: string;
    createdAt: string;
    updatedAt?: string;
    author: UserDto;
    likes: number;
    commentsCount: number;
}