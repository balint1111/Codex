import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from '../auth.service';

/**
 * Generic service providing CRUD operations against the backend.
 * Child services specify the resource path and model type.
 */
export class ApiService<T> {
  constructor(
    protected http: HttpClient,
    protected auth: AuthService,
    private resource: string
  ) {}

  list(): Observable<T[]> {
    return this.http.get<T[]>(`${this.auth.API_URL}/${this.resource}`, {
      headers: this.auth.authHeaders()
    });
  }

  get(id: number): Observable<T> {
    return this.http.get<T>(`${this.auth.API_URL}/${this.resource}/${id}`, {
      headers: this.auth.authHeaders()
    });
  }

  create(body: Partial<T>): Observable<T> {
    return this.http.post<T>(`${this.auth.API_URL}/${this.resource}`, body, {
      headers: this.auth.authHeaders()
    });
  }

  update(id: number, body: Partial<T>): Observable<T> {
    return this.http.put<T>(`${this.auth.API_URL}/${this.resource}/${id}`, body, {
      headers: this.auth.authHeaders()
    });
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.auth.API_URL}/${this.resource}/${id}`, {
      headers: this.auth.authHeaders()
    });
  }
}
