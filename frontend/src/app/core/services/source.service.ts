import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Source, SourceRequest, ResourceCategory, ResourceCategoryRequest } from '../models/source.model';
import { PagedResponse } from '../models/page.model';
import { environment } from '../../../environments/environment';

@Injectable({
    providedIn: 'root'
})
export class SourceService {
    private http = inject(HttpClient);
    private readonly apiUrl = `${environment.apiBaseUrl}/sources`;
    private readonly categoryUrl = `${environment.apiBaseUrl}/resource-categories`;

    // ── Sources ──────────────────────────────────────────────────────────────

    getAllSources(): Observable<Source[]> {
        return this.http.get<PagedResponse<Source>>(this.apiUrl, { params: { size: '100' } })
            .pipe(map(res => res.content));
    }

    getSourceById(id: number): Observable<Source> {
        return this.http.get<Source>(`${this.apiUrl}/${id}`);
    }

    createSource(request: SourceRequest): Observable<Source> {
        return this.http.post<Source>(this.apiUrl, request);
    }

    updateSource(id: number, request: SourceRequest): Observable<Source> {
        return this.http.put<Source>(`${this.apiUrl}/${id}`, request);
    }

    deleteSource(id: number): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${id}`);
    }

    // ── Resource Categories ───────────────────────────────────────────────────

    getCategories(): Observable<ResourceCategory[]> {
        return this.http.get<ResourceCategory[]>(this.categoryUrl);
    }

    getAllCategories(): Observable<ResourceCategory[]> {
        return this.http.get<ResourceCategory[]>(`${this.categoryUrl}/all`);
    }

    createCategory(request: ResourceCategoryRequest): Observable<ResourceCategory> {
        return this.http.post<ResourceCategory>(this.categoryUrl, request);
    }

    updateCategory(id: number, request: ResourceCategoryRequest): Observable<ResourceCategory> {
        return this.http.put<ResourceCategory>(`${this.categoryUrl}/${id}`, request);
    }

    deleteCategory(id: number): Observable<void> {
        return this.http.delete<void>(`${this.categoryUrl}/${id}`);
    }
}
