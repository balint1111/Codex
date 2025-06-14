import { Privilege } from './privilege';

export interface User {
  id: number;
  username: string;
  privileges: Privilege[];
}
