/** A six-digit code, as the computer shows it: {@code 382615} reads {@code 382 615}. */
export function spacedCode(code: string): string {
  return /^\d{6}$/.test(code) ? `${code.slice(0, 3)} ${code.slice(3)}` : code;
}

/** What the learner typed, digits only; null until there are six. */
export function typedCode(text: string): string | null {
  const digits = text.replace(/\D/g, '');
  return digits.length === 6 ? digits : null;
}
