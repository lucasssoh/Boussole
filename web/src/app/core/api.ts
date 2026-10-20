/** The types of the HTTP API of boussole-server, one for each of its views. */

export type SessionKind = 'lesson' | 'review' | 'exam';
export type Verdict = 'correct' | 'partly' | 'wrong';

export interface CourseSummary {
  id: string;
  name: string;
  lang: string;
  description: string | null;
  planned: number;
  written: number;
  finished: number;
}

export interface Course {
  id: string;
  name: string;
  lang: string;
  description: string | null;
  sourcesDir: string;
  chapters: Chapter[];
  lexicon: LexiconTerm[];
}

export interface Chapter {
  id: string;
  title: string;
  sessions: SessionEntry[];
}

export interface SessionEntry {
  id: string;
  title: string;
  kind: SessionKind;
  duration: number;
  goal: string;
  written: boolean;
  finished: boolean;
}

export interface LexiconTerm {
  id: string;
  term: string;
  aliases: string[];
  definition: string;
  see: string[];
}

export interface Session {
  subject: string;
  id: string;
  title: string;
  kind: SessionKind;
  duration: number;
  goal: string;
  chapterId: string;
  chapterTitle: string;
  file: string;
  objectives: string[];
  prerequisites: string[];
  sources: { path: string; page: number | null }[];
  parts: SessionPart[];
  questionMinutes: number;
  previous: string | null;
  next: string | null;
}

export interface SessionPart {
  kind: 'text' | 'warmup' | 'notion' | 'checkpoint' | 'practice';
  id: string | null;
  title: string | null;
  blocks: BlockView[];
}

export type BlockView =
  | ProseBlockView
  | ExampleBlockView
  | RecallBlockView
  | CalloutBlockView
  | VideoBlockView
  | PdfBlockView
  | McqBlockView
  | ExerciseBlockView
  | MissingRefBlockView;

export interface ProseBlockView {
  type: 'prose';
  markdown: string;
}

export interface ExampleBlockView {
  type: 'example';
  label: string;
  blocks: BlockView[];
}

export interface RecallBlockView {
  type: 'recall';
  blocks: BlockView[];
}

export interface CalloutBlockView {
  type: 'callout';
  kind: 'note' | 'tip' | 'warning';
  blocks: BlockView[];
}

export interface VideoBlockView {
  type: 'video';
  label: string;
  youtubeId: string;
  start: number | null;
  end: number | null;
}

export interface PdfBlockView {
  type: 'pdf';
  label: string;
  path: string;
  page: number | null;
}

export interface QuestionHints {
  total: number;
  revealed: BlockView[][];
}

export interface QuestionProgress {
  attempts: number;
  solved: boolean;
  lastVerdict: Verdict | null;
}

interface QuestionView {
  id: string;
  statement: BlockView[];
  minutes: number;
  hints: QuestionHints;
  progress: QuestionProgress;
  reused: boolean;
}

export interface McqBlockView extends QuestionView {
  type: 'mcq';
  multiple: boolean;
  choices: string[];
}

export interface ExerciseBlockView extends QuestionView {
  type: 'exercise';
  exerciseType: 'numeric' | 'text' | 'fill' | 'order' | 'match' | 'free';
  input: ExerciseInput;
}

export interface MissingRefBlockView {
  type: 'missingRef';
  id: string;
}

export type ExerciseInput =
  | { kind: 'number'; unit: string | null }
  | { kind: 'text' }
  | { kind: 'fill'; gaps: FillGapInput[] }
  | { kind: 'order'; items: string[] }
  | { kind: 'match'; lefts: string[]; rights: string[] }
  | { kind: 'free' }
  | { kind: 'unreadable' };

export interface FillGapInput {
  number: number;
  kind: 'text' | 'number' | 'choice';
  options: string[] | null;
  unit: string | null;
}

export type AttemptRequest =
  | { kind: 'choices'; picked: number[] }
  | { kind: 'value'; value: string }
  | { kind: 'gaps'; values: Record<number, string> }
  | { kind: 'order'; items: string[] }
  | { kind: 'match'; pairs: Record<string, string> }
  | { kind: 'selfGrade'; grade: 'got_it' | 'partly' | 'not_yet' };

export interface CheckedItem {
  key: string;
  correct: boolean;
  feedback: string | null;
}

export interface AttemptResult {
  verdict: Verdict;
  items: CheckedItem[];
  progress: QuestionProgress;
}

export interface RevealedHint {
  number: number;
  total: number;
  blocks: BlockView[];
}

export type ExpectedAnswer =
  | { kind: 'choices'; choices: { correct: boolean; feedback: string | null }[] }
  | { kind: 'value'; value: string }
  | { kind: 'gaps'; values: Record<string, string> }
  | { kind: 'order'; items: string[] }
  | { kind: 'pairs'; pairs: [string, string][] }
  | { kind: 'nothing' };

export interface RevealedSolution {
  blocks: BlockView[];
  expected: ExpectedAnswer;
}

export interface ServerInfo {
  name: string;
  version: string;
  language: string | null;
}
