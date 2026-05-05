import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PorterPendingOrdersService {
  private _orders = new BehaviorSubject<any[]>([]);
  orders$ = this._orders.asObservable();

  private readonly apiBase = 'https://carrygo-production.up.railway.app/api';

  constructor(private http: HttpClient) {}

  get orders(): any[] { return this._orders.getValue(); }
  get count(): number { return this._orders.getValue().length; }

  /** Fetch dispatched PENDING deliveries and replace shared state. */
  load(userId: number): void {
    this.http.get<any[]>(`${this.apiBase}/deliveries/matched/${userId}`)
      .pipe(catchError(() => of([])))
      .subscribe(list => {
        this._orders.next(
          (list as any[]).filter(o => o.pickupAddress?.trim() && o.dropAddress?.trim())
        );
      });
  }

  add(order: any): void {
    const current = this._orders.getValue();
    if (!current.some((o: any) => o.deliveryId === order.deliveryId)) {
      this._orders.next([order, ...current]);
    }
  }

  remove(deliveryId: number): void {
    this._orders.next(this._orders.getValue().filter((o: any) => o.deliveryId !== deliveryId));
  }

  clear(): void {
    this._orders.next([]);
  }
}
