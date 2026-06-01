import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// TypeScript shape of a user / porter as returned by the backend.
export interface PorterProfile {
  userId: number;
  name: string;
  email: string;
  phone: string;
  vehicleType?: string;
  vehicleNumber?: string;
  licenceNumber?: string;
  licenceExpiry?: string;
  role: string;
  isOnline?: boolean;
  avgRating?: number | null;
}

// TypeScript shape of a wallet response.
export interface WalletData {
  walletId: number;
  userId: number;
  balance: number;
  lastUpdated: string;
}

export interface PorterStatus {
  userId: number;
  isOnline: boolean;
  updatedAt: string;
}

// Wraps every backend call related to users, profiles, and wallets.
// Components inject this service and call its methods instead of using HttpClient directly.
@Injectable({ providedIn: 'root' })
export class UserService {
  private baseUrl = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // Look up a porter by email — used on the porter profile screen.
  getPorterProfileByEmail(email: string): Observable<PorterProfile> {
    return this.http.get<PorterProfile>(`${this.baseUrl}/users/email/${email}`);
  }

  // Look up any user by their ID.
  getPorterProfileById(userId: number): Observable<PorterProfile> {
    return this.http.get<PorterProfile>(`${this.baseUrl}/users/${userId}`);
  }

  // Get the user's wallet balance.
  getWalletByUserId(userId: number): Observable<WalletData> {
    return this.http.get<WalletData>(`${this.baseUrl}/wallets/user/${userId}`);
  }

  // Porter toggles their availability so they start/stop receiving ride requests.
  updatePorterStatus(userId: number, isOnline: boolean): Observable<PorterProfile> {
    return this.http.put<PorterProfile>(`${this.baseUrl}/users/${userId}/status`, {
      is_online: isOnline
    });
  }

  // List deliveries the user has sent.
  getPorterDeliveries(userId: number, limit: number = 10): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/deliveries/user/${userId}`);
  }

  // List deliveries currently waiting for a porter to accept.
  getAvailableDeliveries(): Observable<any[]> {
    return this.http.get<any[]>(`${this.baseUrl}/deliveries/available`);
  }

  // Patch the user's profile / KYC fields.
  updatePorterProfile(userId: number, profileData: Partial<PorterProfile>): Observable<PorterProfile> {
    return this.http.put<PorterProfile>(`${this.baseUrl}/users/${userId}`, profileData);
  }

  getAllUsers(): Observable<PorterProfile[]> {
    return this.http.get<PorterProfile[]>(`${this.baseUrl}/users`);
  }
}
