import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SourceService } from '../../../core/services/source.service';
import { Source, SourceRequest, ResourceCategory, ResourceCategoryRequest, TrackingType } from '../../../core/models/source.model';
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
    categories: ResourceCategory[] = [];

    sourceForm: FormGroup;
    categoryForm: FormGroup;

    editingSourceId: number | null = null;
    editingCategoryId: number | null = null;

    showSourceForm = false;
    showCategoryForm = false;
    showCategoryPanel = false;

    user$ = this.authService.currentUser$;

    readonly trackingTypes: TrackingType[] = ['LINEAR', 'BINARY'];

    constructor() {
        this.sourceForm = this.fb.group({
            title:         ['', Validators.required],
            categoryId:    [null, Validators.required],
            trackingType:  ['LINEAR', Validators.required],
            totalCapacity: [null, Validators.required],
            dailyLimit:    [null, Validators.required],
            language:      [''],
            part:          ['']
        });

        this.categoryForm = this.fb.group({
            name:        ['', Validators.required],
            description: ['']
        });
    }

    ngOnInit(): void {
        this.loadSources();
        this.loadCategories();
    }

    loadSources(): void {
        this.sources$ = this.sourceService.getAllSources();
    }

    loadCategories(): void {
        this.sourceService.getCategories().subscribe(cats => this.categories = cats);
    }

    // ── Sources ────────────────────────────────────────────────────────────

    startAdd(): void {
        this.editingSourceId = null;
        this.sourceForm.reset({ trackingType: 'LINEAR' });
        this.showSourceForm = true;
    }

    editSource(source: Source): void {
        this.editingSourceId = source.id;
        this.sourceForm.patchValue({
            title:         source.title,
            categoryId:    source.categoryId,
            trackingType:  source.trackingType,
            totalCapacity: source.totalCapacity,
            dailyLimit:    source.dailyLimit,
            language:      source.language,
            part:          source.part
        });
        this.showSourceForm = true;
    }

    cancelSourceEdit(): void {
        this.showSourceForm = false;
        this.editingSourceId = null;
        this.sourceForm.reset();
    }

    saveSource(): void {
        if (this.sourceForm.invalid) return;
        const request: SourceRequest = this.sourceForm.value;

        if (this.editingSourceId) {
            this.sourceService.updateSource(this.editingSourceId, request).subscribe({
                next: () => { this.loadSources(); this.cancelSourceEdit(); }
            });
        } else {
            this.sourceService.createSource(request).subscribe({
                next: () => { this.loadSources(); this.cancelSourceEdit(); }
            });
        }
    }

    deleteSource(id: number): void {
        if (confirm('Are you sure you want to delete this resource?')) {
            this.sourceService.deleteSource(id).subscribe(() => this.loadSources());
        }
    }

    // ── Categories ─────────────────────────────────────────────────────────

    toggleCategoryPanel(): void {
        this.showCategoryPanel = !this.showCategoryPanel;
        if (this.showCategoryPanel) {
            this.sourceService.getAllCategories().subscribe(cats => this.categories = cats);
        }
    }

    startAddCategory(): void {
        this.editingCategoryId = null;
        this.categoryForm.reset();
        this.showCategoryForm = true;
    }

    editCategory(cat: ResourceCategory): void {
        this.editingCategoryId = cat.id;
        this.categoryForm.patchValue({ name: cat.name, description: cat.description });
        this.showCategoryForm = true;
    }

    cancelCategoryEdit(): void {
        this.showCategoryForm = false;
        this.editingCategoryId = null;
        this.categoryForm.reset();
    }

    saveCategory(): void {
        if (this.categoryForm.invalid) return;
        const request: ResourceCategoryRequest = this.categoryForm.value;

        if (this.editingCategoryId) {
            this.sourceService.updateCategory(this.editingCategoryId, request).subscribe({
                next: () => { this.loadCategoriesForPanel(); this.cancelCategoryEdit(); }
            });
        } else {
            this.sourceService.createCategory(request).subscribe({
                next: () => { this.loadCategoriesForPanel(); this.cancelCategoryEdit(); }
            });
        }
    }

    deleteCategory(id: number): void {
        if (confirm('Deactivate this category? Existing resources will not be affected.')) {
            this.sourceService.deleteCategory(id).subscribe(() => this.loadCategoriesForPanel());
        }
    }

    private loadCategoriesForPanel(): void {
        this.sourceService.getAllCategories().subscribe(cats => this.categories = cats);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    getCategoryName(categoryId: number | null): string {
        if (!categoryId) return '—';
        return this.categories.find(c => c.id === categoryId)?.name ?? '—';
    }

    getTrackingIcon(type: TrackingType | null): string {
        return type === 'BINARY' ? '🎯' : '📈';
    }
}
