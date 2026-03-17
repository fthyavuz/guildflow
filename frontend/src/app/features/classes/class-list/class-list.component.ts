import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { ClassService } from '../../../core/services/class.service';
import { AuthService } from '../../../core/services/auth.service';
import { ClassResponse } from '../../../core/models/class.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
    selector: 'app-class-list',
    standalone: true,
    imports: [CommonModule, RouterModule, TranslateModule],
    templateUrl: './class-list.component.html',
    styleUrl: './class-list.component.css'
})
export class ClassListComponent implements OnInit {
    private classService = inject(ClassService);
    private authService = inject(AuthService);

    classes$: Observable<ClassResponse[]> | undefined;
    user$ = this.authService.currentUser$;

    ngOnInit(): void {
        this.classes$ = this.classService.getClasses();
    }
}
