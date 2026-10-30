import { spacedCode, typedCode } from './pairing-code';

describe('pairing code', () => {
  it('shows six digits in two groups', () => {
    expect(spacedCode('382615')).toBe('382 615');
    expect(spacedCode('4821')).toBe('4821');
  });

  it('reads a code typed with spaces or dashes, once it has six digits', () => {
    expect(typedCode(' 382 615 ')).toBe('382615');
    expect(typedCode('382-61')).toBeNull();
  });
});
