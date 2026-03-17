import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Source, SourceRequest } from '../models/source.model';

@Injectable({
    providedIn: 'root'
})
export class SourceService {
    private http = inject(HttpClient);
    private readonly apiUrl = 'http://localhost:8080/api/sources';

    getAllSources(): Observable<Source[]> {
        return this.http.get<Source[]>(this.apiUrl);
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
}
