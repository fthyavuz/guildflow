import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { UserResponse } from '../../../core/models/auth.model';
import { Observable } from 'rxjs';
import { TranslateModule } from '@ngx-translate/core';

@Component({
  selector: 'app-mentor-list',
  imports: [CommonModule, RouterModule, TranslateModule],
  templateUrl: './mentor-list.component.html',
  styleUrl: './mentor-list.component.css'
})
export class MentorListComponent implements OnInit {
  private userService = inject(UserService);

  mentors$: Observable<UserResponse[]> | undefined;

  ngOnInit(): void {
    this.mentors$ = this.userService.getMentors();
  }
}
