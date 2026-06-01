import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Backend calls for the wallet screen (balance + top-up + deduct).
@Injectable({ providedIn: 'root' })
export class Wallet {
  private apiUrl = 'http://localhost:8081/api/wallets';

  constructor(private http: HttpClient) {}

  // Current wallet (balance + last-updated timestamp).
  getWalletByUserId(userId: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/user/${userId}`);
  }

  // Alias used by some components.
  getBalance(userId: number): Observable<any> {
    return this.getWalletByUserId(userId);
  }

  // Add money to the wallet.
  topUp(userId: number, amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/user/${userId}/topup`, { amount });
  }

  // Remove money from the wallet (e.g. when paying for a delivery).
  deduct(userId: number, amount: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/user/${userId}/deduct`, { amount });
  }
}
