import { ReviewStatusType, RoleType } from "./db/schema";
import { User } from "./user.types";

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
  phraseId: string;
  status: ReviewStatusType;
  user: User;
}

export interface GlossaryUser {
  code: string;
  published: boolean;
  user: User;
  role: RoleType;
}

export interface PendingPhrase {
  phrase: Phrase;
  user: User;
  original: Phrase | null;
  reviews: PhraseReview[];
}
