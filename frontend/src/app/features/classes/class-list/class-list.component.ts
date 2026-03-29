import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { ClassService } from '../../../core/services/class.service';
import { AuthService } from '../../../core/services/auth.service';
import { ClassResponse } from '../../../core/models/class.model';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-class-list',
    standalone: true,
    imports: [CommonModule, RouterModule, FormsModule, TranslateModule],
    templateUrl: './class-list.component.html',
    styleUrl: './class-list.component.css'
})
export class ClassListComponent implements OnInit, OnDestroy {
    private classService = inject(ClassService);
    private authService = inject(AuthService);
    private destroy$ = new Subject<void>();

    user$ = this.authService.currentUser$;

    allClasses: ClassResponse[] = [];
    filteredClasses: ClassResponse[] = [];
    isLoading = false;

    searchName = '';
    selectedLevel = '';
    selectedInstructor = '';

    educationLevels: string[] = [];
    instructors: string[] = [];

    ngOnInit(): void {
        this.isLoading = true;
        this.classService.getClasses()
            .pipe(takeUntil(this.destroy$))
            .subscribe({
                next: (classes) => {
                    this.allClasses = classes;
                    this.buildFilterOptions(classes);
                    this.applyFilters();
                    this.isLoading = false;
                },
                error: () => { this.isLoading = false; }
            });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    private buildFilterOptions(classes: ClassResponse[]): void {
        this.educationLevels = [...new Set(classes.map(c => c.educationLevel).filter(Boolean))].sort();
        this.instructors = [...new Set(classes.map(c => c.mentorName).filter(Boolean))].sort() as string[];
    }

    applyFilters(): void {
        const name = this.searchName.toLowerCase().trim();
        const level = this.selectedLevel;
        const instructor = this.selectedInstructor;

        this.filteredClasses = this.allClasses.filter(cls => {
            const matchName = !name || cls.name.toLowerCase().includes(name);
            const matchLevel = !level || cls.educationLevel === level;
            const matchInstructor = !instructor || cls.mentorName === instructor;
            return matchName && matchLevel && matchInstructor;
        });
    }

    clearFilters(): void {
        this.searchName = '';
        this.selectedLevel = '';
        this.selectedInstructor = '';
        this.applyFilters();
    }

    get hasActiveFilters(): boolean {
        return !!(this.searchName || this.selectedLevel || this.selectedInstructor);
    }
}
