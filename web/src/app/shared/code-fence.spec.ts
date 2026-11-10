import { codeText, parseFenceInfo, renderCodeFence, splitLines } from './code-fence';

const escape = (text: string) => text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');

describe('parseFenceInfo', () => {
  it('reads the language, the numbers and the marked lines', () => {
    expect(parseFenceInfo('java')).toEqual({ language: 'java', numbered: false, marked: new Set() });
    expect(parseFenceInfo('Java {numbers}')).toEqual({ language: 'java', numbered: true, marked: new Set() });
    expect(parseFenceInfo('python {3,5-6}')).toEqual({ language: 'python', numbered: true, marked: new Set([3, 5, 6]) });
    expect(parseFenceInfo('{2}')).toEqual({ language: '', numbered: true, marked: new Set([2]) });
    expect(parseFenceInfo('')).toEqual({ language: '', numbered: false, marked: new Set() });
  });
});

describe('splitLines', () => {
  it('closes and reopens a span that runs over several lines', () => {
    expect(splitLines('a<span class="c">/* un\ndeux */</span>b')).toEqual(['a<span class="c">/* un</span>', '<span class="c">deux */</span>b']);
  });
});

describe('renderCodeFence', () => {
  it('colours a known language, one span per line, the marked line marked', () => {
    const html = renderCodeFence('int x = 1;\nreturn x;\n', 'java {2}', false, escape);

    expect(html).toContain('<pre class="code numbered hljs" data-language="java">');
    expect(html).toContain('<span class="hljs-type">int</span>');
    expect(html.match(/class="line/g)).toHaveLength(2);
    expect(html).toContain('<span class="line marked"><span class="hljs-keyword">return</span> x;</span>');
  });

  it('shows an unknown language as plain text, escaped', () => {
    const html = renderCodeFence('a < b\n', 'opl', false, escape);

    expect(html).toContain('<pre class="code" data-language="opl"><code><span class="line">a &lt; b</span></code></pre>');
  });

  it('keeps the gaps of a fill exercise inside coloured code', () => {
    const html = renderCodeFence('String s = "[[1]]";\nint n = [[2]];\n', 'java', true, escape);

    expect(html).toContain('<span class="gap" data-gap="1"></span>');
    expect(html).toContain('<span class="gap" data-gap="2"></span>');
    expect(html).not.toContain('[[');
  });

  it('copies the code as written, without numbers', () => {
    const block = document.createElement('div');
    block.innerHTML = renderCodeFence('a = 1\n\nb = 2\n', 'python {numbers}', false, escape);

    expect(codeText(block)).toBe('a = 1\n\nb = 2');
  });
});
