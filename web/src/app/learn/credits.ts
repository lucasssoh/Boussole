import { Authorship } from '../core/api';

/** Who wrote a session, in one line: « Écrite par claude-opus-5-5 (Claude Code), le 21/10/2026 · relue par Jeanne Martin ». */
export function authorshipLine(writtenBy: Authorship[], translate: (key: string, values?: Record<string, unknown>) => string, locale: string): string {
  return writtenBy.map((author, index) => {
    const who = author.model ?? author.name ?? '';
    const tool = author.tool ? ` (${author.tool})` : '';
    const date = author.date ? ', ' + translate('credits.on', { date: new Date(author.date + 'T12:00:00').toLocaleDateString(locale) }) : '';
    const verb = translate(`credits.${author.role}${index === 0 ? '' : 'Later'}`);
    return `${verb} ${who}${tool}${date}`;
  }).join(' · ');
}
