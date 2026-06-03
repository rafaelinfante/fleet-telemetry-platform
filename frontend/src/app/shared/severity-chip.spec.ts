import { provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { SeverityChipComponent } from './severity-chip';

describe('SeverityChipComponent', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [SeverityChipComponent],
      providers: [provideZonelessChangeDetection()],
    });
  });

  it('renders the severity label with a matching css class', () => {
    const fixture = TestBed.createComponent(SeverityChipComponent);
    fixture.componentRef.setInput('severity', 'CRITICAL');
    fixture.detectChanges();

    const chip = fixture.nativeElement.querySelector('.severity-chip') as HTMLElement;
    expect(chip.textContent?.trim()).toBe('CRITICAL');
    expect(chip.classList).toContain('severity-critical');
  });
});
