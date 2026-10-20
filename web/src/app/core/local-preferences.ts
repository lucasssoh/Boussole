/** Small preferences of this browser (theme, language). Storage may be refused: they then last the visit only. */

export function storedValue<T extends string>(key: string, allowed: readonly T[], fallback: T): T {
  try {
    const value = localStorage.getItem(`boussole.${key}`);
    return allowed.find((candidate) => candidate === value) ?? fallback;
  } catch {
    return fallback;
  }
}

/** Free text kept in this browser, empty when there is none. */
export function storedText(key: string): string {
  try {
    return localStorage.getItem(`boussole.${key}`) ?? '';
  } catch {
    return '';
  }
}

export function storeValue(key: string, value: string): void {
  try {
    localStorage.setItem(`boussole.${key}`, value);
  } catch {
    // Kept for this visit only.
  }
}
