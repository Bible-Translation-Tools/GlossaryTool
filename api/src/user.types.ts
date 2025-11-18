export interface TokenRes {
  id: number;
  name: string;
  sha1: string;
  token_last_eight: string;
  scopes: Array<string>;
}

export interface UserRes {
  id: number;
  login: string;
  email: string;
  username: string;
}
