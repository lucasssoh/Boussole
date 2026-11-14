/**
 * A field the learner types in, where the arrow keys move the caret and must not change step.
 * The choices of a question are not: on them, the arrow keys change step as anywhere else.
 */
export function typingIn(target: EventTarget | null): boolean {
  const field = target instanceof Element ? target.closest('input, textarea, select, [contenteditable]') : null;
  return field !== null && !(field instanceof HTMLInputElement && (field.type === 'radio' || field.type === 'checkbox'));
}

/**
 * The arrow keys never move the pick of a question, as a radio button does by itself: the
 * answer sent is always the one the learner pressed, even after an arrow key meant to scroll
 * or to change step.
 */
export function holdChoice(event: KeyboardEvent): void {
  if (event.key.startsWith('Arrow')) {
    event.preventDefault();
  }
}
