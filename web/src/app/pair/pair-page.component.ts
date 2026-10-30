import { ChangeDetectionStrategy, Component, DestroyRef, inject, input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { TranslocoPipe } from '@jsverse/transloco';
import { PairingDevice } from '../core/api';
import { BoussoleApi } from '../core/boussole-api';
import { spacedCode, typedCode } from './pairing-code';

type Step = 'choose' | 'confirm' | 'waiting' | 'done';

/** How often a device that asked looks whether the owner accepted. */
const POLL_MS = 2000;

/**
 * Pairs this device with the Boussole of a computer: with the code the computer shows (typed, or
 * in the address its QR code opens), or, without a keyboard, by showing a code for the owner to
 * accept on the computer.
 */
@Component({
  selector: 'app-pair-page',
  imports: [FormsModule, TranslocoPipe],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <main class="page pair">
      <h1>{{ 'pair.title' | transloco }}</h1>
      @switch (step()) {
        @case ('choose') {
          <div class="pair-ways">
            <form class="frame pair-way" (ngSubmit)="check()">
              <label for="pair-code" class="pair-way-title">{{ 'pair.code' | transloco }}</label>
              <input id="pair-code" name="code" type="text" inputmode="numeric" autocomplete="one-time-code" placeholder="000 000" [(ngModel)]="typed" />
              <button type="submit" class="primary" [disabled]="busy() || !ready()">{{ 'pair.check' | transloco }}</button>
            </form>
            <section class="frame pair-way">
              <h2 class="pair-way-title">{{ 'pair.noKeyboard' | transloco }}</h2>
              <button type="button" [disabled]="busy()" (click)="ask()">{{ 'pair.showCode' | transloco }}</button>
            </section>
          </div>
        }
        @case ('confirm') {
          @if (device(); as found) {
            <dl class="frame pair-device">
              <div><dt>{{ 'pair.device' | transloco }}</dt><dd>{{ found.name }}</dd></div>
              <div><dt>{{ 'pair.address' | transloco }}</dt><dd>{{ found.address }}</dd></div>
              <div><dt>{{ 'pair.access' | transloco }}</dt><dd>{{ 'pair.' + found.access | transloco }}</dd></div>
            </dl>
          }
          <div class="pair-actions">
            <button type="button" class="primary" [disabled]="busy()" (click)="claim()">{{ 'pair.pair' | transloco }}</button>
            <button type="button" [disabled]="busy()" (click)="restart()">{{ 'pair.cancel' | transloco }}</button>
          </div>
        }
        @case ('waiting') {
          <section class="frame pair-waiting">
            <h2 class="pair-way-title">{{ 'pair.thisCode' | transloco }}</h2>
            <p class="pair-shown-code">{{ shownCode() }}</p>
            <p class="note">{{ 'pair.waiting' | transloco }}</p>
          </section>
          <div class="pair-actions">
            <button type="button" (click)="restart()">{{ 'pair.cancel' | transloco }}</button>
          </div>
        }
        @case ('done') {
          <p class="verdict correct">{{ 'pair.done' | transloco }}</p>
          <div class="pair-actions">
            <button type="button" class="primary" (click)="enter()">{{ 'pair.continue' | transloco }}</button>
          </div>
        }
      }
      @if (problem(); as key) {
        <p class="frame warning" role="alert">{{ key | transloco }}</p>
        @if (step() === 'choose') {
          <div class="pair-actions">
            <button type="button" (click)="restart()">{{ 'pair.again' | transloco }}</button>
          </div>
        }
      }
    </main>
  `,
})
export class PairPageComponent implements OnInit {
  /** The code the QR code's address carries, {@code /pair?code=382615}. */
  readonly code = input<string | undefined>(undefined);

  private readonly api = inject(BoussoleApi);
  private timer: ReturnType<typeof setTimeout> | null = null;

  protected readonly step = signal<Step>('choose');
  protected readonly busy = signal(false);
  protected readonly device = signal<PairingDevice | null>(null);
  protected readonly shownCode = signal('');
  protected readonly problem = signal<string | null>(null);
  protected typed = '';
  private checked: string | null = null;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.stopPolling());
  }

  ngOnInit(): void {
    const fromQr = this.code();
    if (fromQr) {
      this.typed = spacedCode(fromQr);
      void this.check();
    }
  }

  protected ready(): boolean {
    return typedCode(this.typed) !== null;
  }

  protected async check(): Promise<void> {
    const code = typedCode(this.typed);
    if (!code) {
      return;
    }
    await this.work(async () => {
      this.device.set(await this.api.previewPairing(code));
      this.checked = code;
      this.step.set('confirm');
    }, 'pair.refused');
  }

  protected async claim(): Promise<void> {
    const code = this.checked;
    if (!code) {
      return;
    }
    await this.work(async () => {
      this.device.set(await this.api.claimPairing(code));
      this.step.set('done');
    }, 'pair.refused');
  }

  protected async ask(): Promise<void> {
    await this.work(async () => {
      const made = await this.api.requestPairing();
      this.shownCode.set(spacedCode(made.code));
      this.step.set('waiting');
      this.poll(made.id);
    }, 'pair.unreachable');
  }

  protected restart(): void {
    this.stopPolling();
    this.problem.set(null);
    this.device.set(null);
    this.checked = null;
    this.typed = '';
    this.step.set('choose');
  }

  /** A full load, so that everything the app asked before pairing is asked again. */
  protected enter(): void {
    window.location.assign('/');
  }

  private poll(id: string): void {
    this.timer = setTimeout(async () => {
      try {
        const status = await this.api.pairingStatus(id);
        if (status.state === 'pending') {
          this.poll(id);
        } else if (status.state === 'accepted') {
          this.device.set(status.device);
          this.step.set('done');
        } else {
          this.step.set('choose');
          this.problem.set(status.state === 'denied' ? 'pair.denied' : 'pair.expired');
        }
      } catch {
        this.step.set('choose');
        this.problem.set('pair.unreachable');
      }
    }, POLL_MS);
  }

  private stopPolling(): void {
    if (this.timer) {
      clearTimeout(this.timer);
      this.timer = null;
    }
  }

  private async work(action: () => Promise<void>, failure: string): Promise<void> {
    this.busy.set(true);
    this.problem.set(null);
    try {
      await action();
    } catch (error) {
      this.problem.set((error as { status?: number })?.status === 409 ? failure : 'pair.unreachable');
    } finally {
      this.busy.set(false);
    }
  }
}
