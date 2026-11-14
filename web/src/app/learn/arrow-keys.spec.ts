import { holdChoice, typingIn } from './arrow-keys';

describe('arrow keys', () => {
  function element(html: string): Element {
    const host = document.createElement('div');
    host.innerHTML = html;
    return host.firstElementChild!;
  }

  it('leave the arrow keys to a field the learner types in', () => {
    expect(typingIn(element('<input type="text" />'))).toBe(true);
    expect(typingIn(element('<textarea></textarea>'))).toBe(true);
    expect(typingIn(element('<div contenteditable="true"><span>x</span></div>').firstElementChild)).toBe(true);
  });

  it('change step from a choice of a question, or anywhere else', () => {
    expect(typingIn(element('<input type="radio" />'))).toBe(false);
    expect(typingIn(element('<input type="checkbox" />'))).toBe(false);
    expect(typingIn(element('<p>text</p>'))).toBe(false);
    expect(typingIn(null)).toBe(false);
  });

  it('never let an arrow key move the pick of a radio button', () => {
    const radios = element('<div><input type="radio" name="q" checked /><input type="radio" name="q" /></div>');
    document.body.append(radios);
    const [first] = Array.from(radios.querySelectorAll('input'));
    first.addEventListener('keydown', holdChoice);
    for (const key of ['ArrowDown', 'ArrowUp', 'ArrowLeft', 'ArrowRight']) {
      const event = new KeyboardEvent('keydown', { key, cancelable: true, bubbles: true });
      first.dispatchEvent(event);
      expect(event.defaultPrevented).toBe(true);
    }
    const space = new KeyboardEvent('keydown', { key: ' ', cancelable: true, bubbles: true });
    first.dispatchEvent(space);
    expect(space.defaultPrevented).toBe(false);
    radios.remove();
  });
});
