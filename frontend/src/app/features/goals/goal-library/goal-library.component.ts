import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { GoalService } from '../../../core/services/goal.service';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, tap } from 'rxjs';

@Component({
  selector: 'app-goal-library',
  standalone: true,
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './goal-library.component.html',
  styleUrl: './goal-library.component.css'
})
export class GoalLibraryComponent implements OnInit {
  private goalService = inject(GoalService);
  
  templates$: Observable<any[]> | undefined;

  ngOnInit(): void {
    this.loadTemplates();
  }

  loadTemplates(): void {
    this.templates$ = this.goalService.getTemplates().pipe(
      tap((data: any[]) => console.log('Library Templates received:', data))
    );
  }

  deleteTemplate(id: number): void {
    if (confirm('Are you sure you want to delete this template from the library?')) {
      this.goalService.deleteGoal(id).subscribe({
        next: () => {
          this.loadTemplates();
        },
        error: (err) => {
          console.error('Error deleting template:', err);
        }
      });
    }
  }
}
