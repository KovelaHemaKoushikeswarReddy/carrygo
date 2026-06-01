import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Backend calls for everything related to deliveries (create / list / accept / update status).
// Despite the class name "Delivery", this is a service, not a model.
@Injectable({ providedIn: 'root' })
export class Delivery {
  private apiUrl = 'http://localhost:8081/api/deliveries';

  constructor(private http: HttpClient) {}

  // User books a new delivery.
  createDelivery(data: any): Observable<any> {
    return this.http.post(this.apiUrl, data);
  }

  // All deliveries the user has sent (their history).
  getUserDeliveries(userId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/user/${userId}`);
  }

  // All deliveries currently waiting for a porter to accept.
  getAvailableDeliveries(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/available`);
  }

  // Pending deliveries shown to one specific porter.
  getMatchedDeliveries(porterId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/matched/${porterId}`);
  }

  // Count of online porters — used in the "find a driver" UI to show availability.
  getMatchingPortersCount(
    pickupLat?: number, pickupLng?: number,
    dropLat?: number,   dropLng?: number
  ): Observable<number> {
    let params: any = {};
    if (pickupLat != null) params['pickupLat'] = pickupLat;
    if (pickupLng != null) params['pickupLng'] = pickupLng;
    if (dropLat   != null) params['dropLat']   = dropLat;
    if (dropLng   != null) params['dropLng']   = dropLng;
    return this.http.get<number>(`${this.apiUrl}/matching-porters-count`, { params });
  }

  // Move a delivery to a new status (PICKED_UP, DELIVERED, etc.).
  updateDeliveryStatus(deliveryId: number, status: string): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${deliveryId}/status`, null, { params: { status } });
  }

  // Porter accepts a pending delivery.
  acceptDelivery(deliveryId: number, commuterId: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${deliveryId}/accept`, null, { params: { commuterId } });
  }

  // Fetch one delivery by ID.
  getDeliveryById(deliveryId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${deliveryId}`);
  }

  // All deliveries a porter has accepted.
  getCommuterDeliveries(commuterId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/commuter/${commuterId}`);
  }
}
