import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TranslocoPipe } from '@jsverse/transloco';
import { LearnFacade } from './learn.facade';

/** The home page: every subject of config.yaml. */
@Component({
  selector: 'app-subject-list',
  imports: [RouterLink, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page narrow">
      <h1>{{ 'home.title' | transloco }}</h1>
      @if (learn.unreachable()) {
        <p class="frame warning">{{ 'app.unreachable' | transloco }}</p>
      }
      @if (learn.subjects(); as subjects) {
        @if (subjects.length === 0) {
          <p class="frame note">{{ 'home.empty' | transloco }}</p>
        }
        <ul class="cards">
          @for (subject of subjects; track subject.id) {
            <li>
              <a class="card" [routerLink]="['/subjects', subject.id]" [attr.lang]="subject.lang">
                <span class="card-title">{{ subject.name }}</span>
                @if (subject.description) {
                  <span class="card-text">{{ subject.description }}</span>
                }
                <span class="card-meta">{{ 'home.progress' | transloco: subject }}</span>
                <progress [max]="subject.planned || 1" [value]="subject.finished"></progress>
              </a>
            </li>
          }
        </ul>
      }
    </main>
  `,
})
export class SubjectListComponent {
  protected readonly learn = inject(LearnFacade);

  constructor() {
    void this.learn.loadSubjects().catch(() => undefined);
  }
}
