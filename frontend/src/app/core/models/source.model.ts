export type TrackingType = 'LINEAR' | 'BINARY';

export interface ResourceCategory {
    id: number;
    name: string;
    description?: string;
    active: boolean;
}

export interface ResourceCategoryRequest {
    name: string;
    description?: string;
}

export interface Source {
    id: number;
    title: string;
    categoryId: number | null;
    categoryName: string | null;
    trackingType: TrackingType;
    totalCapacity: number | null;
    dailyLimit: number | null;
    language?: string;
    part?: string;
}

export interface SourceRequest {
    title: string;
    categoryId: number;
    trackingType: TrackingType;
    totalCapacity?: number | null;
    dailyLimit?: number | null;
    language?: string;
    part?: string;
}
