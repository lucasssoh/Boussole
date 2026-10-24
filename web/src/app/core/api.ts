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
  /** When the learner last answered or finished something here, ISO 8601. */
  lastStudied: string | null;
  resume: ResumePoint | null;
  /** The languages the track exists in; {@link lang} is the one shown. */
  languages: string[];
  /** A track shipped inside Boussole: shown apart, and it earns no experience. */
  builtIn: boolean;
  /** The built-in guide a new Boussole opens on, shown beside the session to resume. */
  starter: boolean;
}

/** The session to resume in a subject: the one in progress, or else the next one. */
export interface ResumePoint {
  id: string;
  title: string;
  kind: SessionKind;
  duration: number;
  chapterTitle: string;
}

export interface Course {
  id: string;
  name: string;
  /** The language the track is followed in, one of its {@link languages}. */
  lang: string;
  languages: string[];
  description: string | null;
  sourcesDir: string;
  chapters: Chapter[];
  lexicon: LexiconTerm[];
  resume: ResumePoint | null;
  authors: Credit[];
  contributors: Credit[];
  writtenWith: Writer[];
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
  /** Its questions (those asked again included), how many the learner answered, how many right. */
  questions: number;
  answered: number;
  right: number;
  /** Its prerequisites not finished yet: guided mode opens it once they are, free mode advises them. */
  waitingFor: Prerequisite[];
}

/** A session to finish first: of this track (its title) or of another (its full ID as title). */
export interface Prerequisite {
  subject: string;
  session: string;
  title: string;
}

export interface LexiconTerm {
  id: string;
  term: string;
  aliases: string[];
  definition: string;
  see: string[];
}

/** Who wrote, revised or translated a session: an AI ({@link model}, {@link tool}) or a person ({@link name}). */
export interface Authorship {
  model: string | null;
  tool: string | null;
  name: string | null;
  /** 2026-10-21, or null. */
  date: string | null;
  role: 'wrote' | 'revised' | 'translated';
}

/** An author or a contributor of a track. */
export interface Credit {
  name: string;
  organisation: boolean;
  role: string | null;
  url: string | null;
}

/** A model or a person, and how many sessions of the track they wrote. */
export interface Writer {
  who: string;
  model: boolean;
  sessions: number;
}

export interface Session {
  subject: string;
  /** The language the session is shown in, one of the {@link languages} of its track. */
  lang: string;
  languages: string[];
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
  writtenBy: Authorship[];
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
  | LinkBlockView
  | GraphBlockView
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

/** A video from YouTube ({@link youtubeId}) or a file of the sources ({@link path}); the other is null. */
export interface VideoBlockView {
  type: 'video';
  label: string;
  youtubeId: string | null;
  path: string | null;
  start: number | null;
  end: number | null;
}

/** A web page cited by the course, opened beside the text. */
export interface LinkBlockView {
  type: 'link';
  label: string;
  url: string;
}

/** What Boussole learned of a web page: may it show beside the text, and its title. */
export interface PagePreview {
  reachable: boolean;
  embeddable: boolean;
  title: string | null;
}

/** A small graph drawn by Boussole; places are in grid units, one unit between neighbours. */
export interface GraphBlockView {
  type: 'graph';
  label: string;
  nodes: GraphNodeView[];
  edges: GraphEdgeView[];
  width: number;
  height: number;
}

export interface GraphNodeView {
  id: string;
  /** Its label, or else its ID. */
  text: string;
  x: number;
  y: number;
  emphasized: boolean;
}

export interface GraphEdgeView {
  from: string;
  to: string;
  directed: boolean;
  label: string | null;
  emphasized: boolean;
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
  /** In a lesson, the verdict of the answer already given in it: that answer is final. */
  answeredHere: Verdict | null;
}

interface QuestionView {
  id: string;
  statement: BlockView[];
  minutes: number;
  hints: QuestionHints;
  progress: QuestionProgress;
  reused: boolean;
  /** A guess of the warmup: it earns nothing and is not counted in first try. */
  hook: boolean;
}

export interface McqBlockView extends QuestionView {
  type: 'mcq';
  multiple: boolean;
  choices: string[];
  /** The indexes of choices in the order they are shown; answers keep the indexes of choices. */
  order: number[];
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
  /** The correction, shown right after the answer (at the end of an exam). */
  solution: RevealedSolution;
  /** What the answer was worth, out of what it could earn, before the occasion and the level of the track. */
  earned: number;
  possible: number;
  pointsGained: number;
}

/** A rank: its tier, its division (5 to 1, 0 at the top), its level, and where its division starts and ends. */
export interface Rank {
  tier: string;
  division: number;
  level: number;
  intoLevel: number;
  levelSize: number;
  divisionFrom: number;
  nextAt: number;
  /** The fields still missing to rise above, when the points alone would. */
  fieldsMissing: number;
}

export interface FieldStanding {
  domain: string;
  points: number;
  specialty: Rank;
}

/** Points, the culture rank, the specialty in each field and the streak of days. */
export interface StudyStats {
  points: number;
  culture: Rank;
  fields: FieldStanding[];
  streakDays: number;
  studiedToday: boolean;
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
  /** From config.yaml: a lesson lets the learner go on without answering its questions. */
  freeNavigation: boolean;
}
