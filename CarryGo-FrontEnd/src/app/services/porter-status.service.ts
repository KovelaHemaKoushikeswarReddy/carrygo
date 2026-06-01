import { Injectable } from '@angular/core';

// Keeps the porter's online/offline flag in memory across route changes.
// Always starts as "offline" after a page reload — porters have to toggle online
// each time they open the app.
@Injectable({ providedIn: 'root' })
export class PorterStatusService {

  private _isOnline = false;

  // Placeholder kept so components can call init() after loading the profile.
  init(_userId: number): void { }

  get isOnline(): boolean {
    return this._isOnline;
  }

  // Called when the porter clicks the online/offline toggle.
  set(value: boolean): void {
    this._isOnline = value;
  }
}
