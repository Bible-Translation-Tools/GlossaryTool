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
  author: string;
  sourceLanguage: string;
  targetLanguage: string;
  createdAt: string;
  updatedAt: string;
  resource: Resource;
  phrases: Phrase[];
}
