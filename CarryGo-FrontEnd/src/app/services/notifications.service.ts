import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// One row in the bell-icon notification list.
export interface AppNotification {
  notificationId: number;
  userId: number;
  type: string;
  message: string;
  isRead: boolean;
  createdAt: string;
}

// Backend calls for the bell-icon notification UI.
@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private base = 'http://localhost:8081/api/notifications';

  constructor(private http: HttpClient) {}

  // All notifications for the given user, newest first.
  getForUser(userId: number): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.base}/user/${userId}`);
  }

  // Marks a single notification as read.
  markRead(notificationId: number): Observable<any> {
    return this.http.patch(`${this.base}/${notificationId}/read`, {});
  }
}
