export interface User {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: 'ADMIN' | 'MENTOR' | 'STUDENT' | 'PARENT';
    phone?: string;
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    user: User;
}

export interface UserResponse extends User {
    active: boolean;
    createdAt: string;
}
