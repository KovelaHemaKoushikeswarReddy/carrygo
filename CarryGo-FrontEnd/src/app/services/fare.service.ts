import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Shape of the fare breakdown returned by the backend.
export interface FareEstimate {
  baseFare: number;
  distanceFare: number;
  timeFare: number;
  surgeMultiplier: number;
  surgeLabel: string;
  zoneSurcharge: number;
  totalFare: number;
  fareRange: string;
  distanceKm: number;
  estimatedMinutes: number;
  hasSurge: boolean;
}

// Calls the backend fare estimator before the user confirms a booking.
@Injectable({ providedIn: 'root' })
export class FareService {
  private readonly apiBase = 'http://localhost:8081/api';

  constructor(private http: HttpClient) {}

  // Sends pickup/drop coordinates and vehicle type; backend returns a full price breakdown.
  estimate(
    pickupLat: number, pickupLng: number,
    dropLat: number,   dropLng: number,
    vehicleType = 'auto'
  ): Observable<FareEstimate> {
    return this.http.post<FareEstimate>(`${this.apiBase}/fare/estimate`, {
      pickupLat, pickupLng, dropLat, dropLng, vehicleType
    });
  }
}
