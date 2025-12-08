import { ReviewStatusType } from "./db/schema";

export interface Reference {
  id: string;
  book: string;
  chapter: string;
  verse: string;
}

export interface Phrase {
  id: string;
  phrase: string;
  spelling: string;
  description: string;
  audio: string;
  createdAt: string;
  updatedAt: string;
  refs: Reference[];
}

export interface Resource {
  language: string;
  type: string;
  version: string;
}

export interface Glossary {
  id: string;
  code: string;
  sourceLanguage: string;
  targetLanguage: string;
  createdAt: string;
  updatedAt: string;
  resource: Resource;
  phrases: Phrase[];
}

export interface GlossaryUpdate {
  id: string;
  code: string;
  version: number;
  createdAt: number;
  updatedAt: number;
}

export interface PhraseReview {
  username: string;
  status: ReviewStatusType;
  phraseId: string;
}
