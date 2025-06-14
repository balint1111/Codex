import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth.service';
import { Privilege } from '../models/privilege';
import { ApiService } from './api.service';

@Injectable({ providedIn: 'root' })
export class PrivilegeService extends ApiService<Privilege> {
  constructor(http: HttpClient, auth: AuthService) {
    super(http, auth, 'api/privileges');
  }
}
