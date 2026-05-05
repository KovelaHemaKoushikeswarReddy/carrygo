import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AppNotification {
  notificationId: number;
  userId: number;
  type: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private base = 'https://carrygo-production.up.railway.app/api/notifications';

  constructor(private http: HttpClient) {}

  getForUser(userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.base}/user/${userId}`);
  }

  markRead(notificationId: number): Observable<any> {
    return this.http.patch(`${this.base}/${notificationId}/read`, {});
  }
}
