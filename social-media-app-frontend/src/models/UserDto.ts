    // models/UserDto.ts
    export interface UserDto {
        id?: string;
        username: string;
        bio?: string;
        profilePicture?: string;
        createdAt?: string; // ISO string from backend
    }