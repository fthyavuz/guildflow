export enum SourceType {
    BOOK = 'BOOK',
    PODCAST = 'PODCAST',
    VIDEO = 'VIDEO'
}

export interface Source {
    id: number;
    title: string;
    type: SourceType;
    language?: string;
    part?: string;
    totalPages?: number;
    totalMinutes?: number;
}

export interface SourceRequest {
    title: string;
    type: SourceType;
    language?: string;
    part?: string;
    totalPages?: number;
    totalMinutes?: number;
}
