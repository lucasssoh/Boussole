import { ApplicationRef, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { FillGapInput } from '../../core/api';
import { FillGaps } from '../question/fill-gaps';
import { LearnFacade } from '../learn.facade';
import { WebReferences } from '../web-references';
import { ProseBlockComponent } from './prose-block.component';

describe('ProseBlockComponent', () => {
  it('keeps a fill field focused while its answer changes', () => {
    TestBed.configureTestingModule({
      imports: [ProseBlockComponent],
      providers: [
        { provide: LearnFacade, useValue: { sessionBase: () => '' } },
        { provide: WebReferences, useValue: { preview: () => Promise.resolve(undefined) } },
      ],
    });

    const fixture = TestBed.createComponent(ProseBlockComponent);
    const gaps: FillGapInput[] = [{ number: 1, kind: 'number', options: null, unit: null }];
    const fill = new FillGaps(gaps, signal({}), () => {}, TestBed.inject(ApplicationRef));
    fixture.componentRef.setInput('markdown', 'La valeur est [[1]].');
    fixture.componentRef.setInput('fill', fill);
    fixture.detectChanges();

    const field = fixture.nativeElement.querySelector('input') as HTMLInputElement;
    field.focus();
    field.value = '1';
    field.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    expect(document.activeElement).toBe(field);
    expect(fill.values()[1]).toBe('1');

    fixture.destroy();
  });
});
