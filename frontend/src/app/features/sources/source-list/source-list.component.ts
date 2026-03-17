import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SourceService } from '../../../core/services/source.service';
import { Source, SourceType, SourceRequest } from '../../../core/models/source.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../../core/services/auth.service';

@Component({
    selector: 'app-source-list',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, TranslateModule],
    templateUrl: './source-list.component.html',
    styleUrl: './source-list.component.css'
})
export class SourceListComponent implements OnInit {
    private sourceService = inject(SourceService);
    private fb = inject(FormBuilder);
    private authService = inject(AuthService);

    sources$: Observable<Source[]> | undefined;
    sourceForm: FormGroup;
    editingId: number | null = null;
    showForm = false;
    user$ = this.authService.currentUser$;

    sourceTypes = Object.values(SourceType);

    constructor() {
        this.sourceForm = this.fb.group({
            title: ['', [Validators.required]],
            type: [SourceType.BOOK, [Validators.required]],
            language: [''],
            part: [''],
            totalPages: [null],
            totalMinutes: [null]
        });

        // Watch type changes to reset specific fields
        this.sourceForm.get('type')?.valueChanges.subscribe(type => {
            if (type === SourceType.BOOK) {
                this.sourceForm.get('totalMinutes')?.setValue(null);
            } else {
                this.sourceForm.get('totalPages')?.setValue(null);
            }
        });
    }

    ngOnInit(): void {
        this.loadSources();
    }

    loadSources(): void {
        this.sources$ = this.sourceService.getAllSources();
    }

    startAdd(): void {
        this.editingId = null;
        this.sourceForm.reset({ type: SourceType.BOOK });
        this.showForm = true;
    }

    editSource(source: Source): void {
        this.editingId = source.id;
        this.sourceForm.patchValue(source);
        this.showForm = true;
    }

    cancelEdit(): void {
        this.showForm = false;
        this.editingId = null;
        this.sourceForm.reset();
    }

    saveSource(): void {
        if (this.sourceForm.invalid) return;

        const request: SourceRequest = this.sourceForm.value;

        if (this.editingId) {
            this.sourceService.updateSource(this.editingId, request).subscribe({
                next: () => {
                    this.loadSources();
                    this.cancelEdit();
                }
            });
        } else {
            this.sourceService.createSource(request).subscribe({
                next: () => {
                    this.loadSources();
                    this.cancelEdit();
                }
            });
        }
    }

    deleteSource(id: number): void {
        if (confirm('Are you sure you want to delete this source?')) {
            this.sourceService.deleteSource(id).subscribe(() => this.loadSources());
        }
    }

    getSourceIcon(type: SourceType): string {
        switch (type) {
            case SourceType.BOOK: return '📖';
            case SourceType.PODCAST: return '🎙️';
            case SourceType.VIDEO: return '🎥';
            default: return '📄';
        }
    }
}
