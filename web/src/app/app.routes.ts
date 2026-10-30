import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./learn/subject-list.component').then((m) => m.SubjectListComponent) },
  { path: 'subjects/:subject', loadComponent: () => import('./learn/course-page.component').then((m) => m.CoursePageComponent) },
  {
    path: 'subjects/:subject/sessions/:session',
    loadComponent: () => import('./learn/session-page.component').then((m) => m.SessionPageComponent),
  },
  { path: 'profile', loadComponent: () => import('./progress/profile-page.component').then((m) => m.ProfilePageComponent) },
  { path: 'ranks', loadComponent: () => import('./progress/ranks-page.component').then((m) => m.RanksPageComponent) },
  { path: 'pair', loadComponent: () => import('./pair/pair-page.component').then((m) => m.PairPageComponent) },
  { path: '**', redirectTo: '' },
];
