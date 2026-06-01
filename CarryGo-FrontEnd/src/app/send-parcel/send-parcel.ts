import {
  Component, OnInit, OnDestroy, ChangeDetectorRef,
  Inject, PLATFORM_ID, HostListener, ElementRef
} from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import {FareService} from '../services/fare.service';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { of } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { Delivery } from '../services/delivery/delivery';
import { Wallet } from '../services/wallet/wallet';
import { MapPickerComponent, MapPickerResult } from '../map-picker/map-picker';
import { ChatbotComponent } from '../chatbot/chatbot';

type PackageSize = 'small' | 'medium' | 'large';
type DeliveryType = 'standard' | 'express';

export interface LocationSuggestion {
  address: string;
  lat?: number;
  lng?: number;
  type: 'history' | 'nominatim';
}

const HISTORY_KEY = 'carrygo_location_history';
const MAX_HISTORY  = 10;

// Standalone "Send a Parcel" page. The user picks pickup + drop locations,
// chooses a vehicle and package details, gets a fare estimate, and books the delivery.
@Component({
  selector: 'app-send-parcel',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MapPickerComponent, ChatbotComponent],
  templateUrl: './send-parcel.html',
  styleUrl: './send-parcel.css',
})
export class SendParcelComponent implements OnInit, OnDestroy {

  user: any = {};

  /* ── Pickup ── */
  pickupLocation  = '';
  pickupLat?: number;
  pickupLng?: number;
  contactName     = '';
  contactPhone    = '';

  /* ── Drop ── */
  dropLocation    = '';
  dropLat?: number;
  dropLng?: number;
  receiverName    = '';
  receiverPhone   = '';

  /* ── Package ── */
  packageType     = '';
  weightKg        = 1;
  packageSize: PackageSize = 'medium';
  specialInstructions = '';

  /* ── Delivery prefs ── */
  deliveryType: DeliveryType = 'standard';
  preferredDate   = '';
  preferredTime   = '';
  flexibleMatching = true;

  /* ── Price ── */
  distanceKm      = 0;
  basePrice       = 0;
  distanceCost    = 0;
  serviceFee      = 0;
  totalPrice      = 0;

  /* ── Wallet ── */
  walletBalance     = 0;
  walletError       = '';

  /* ── State ── */
  searched          = false;
  isLoadingCommuters = false;
  isSubmitting      = false;
  showMapPicker     = false;
  /** Real count of online porters whose routes match the entered coordinates. */
  commuterCount     = 0;

  /* ── Autocomplete ── */
  pickupSuggestions: LocationSuggestion[] = [];
  dropSuggestions:   LocationSuggestion[] = [];
  showPickupDrop     = false;
  showDropDrop       = false;
  pickupLoading      = false;
  dropLoading        = false;

  private history:       LocationSuggestion[] = [];
  private pickupSearch$  = new Subject<string>();
  private dropSearch$    = new Subject<string>();
  private subs           = new Subscription();

  packageTypes = [
    { value: 'documents',   label: 'Documents',   icon: '📄' },
    { value: 'electronics', label: 'Electronics', icon: '💻' },
    { value: 'clothing',    label: 'Clothing',    icon: '👗' },
    { value: 'fragile',     label: 'Fragile',     icon: '🧊' },
    { value: 'food',        label: 'Food',        icon: '🍱' },
    { value: 'other',       label: 'Other',       icon: '📦' },
  ];

  packageSizes: { value: PackageSize; label: string; sub: string; icon: string }[] = [
    { value: 'small',  label: 'Small',  sub: 'Up to 30cm',  icon: '📦' },
    { value: 'medium', label: 'Medium', sub: 'Up to 60cm',  icon: '📦' },
    { value: 'large',  label: 'Large',  sub: 'Up to 100cm', icon: '📦' },
  ];

  constructor(
    private authService:     AuthService,
    private deliveryService: Delivery,
    private walletService:   Wallet,
    private fareService:     FareService,
    private router:          Router,
    private http:            HttpClient,
    private cdr:             ChangeDetectorRef,
    private elRef:           ElementRef,
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getCurrentUser() ?? {};
    this.loadHistory();
    this.setupPickupSearch();
    this.setupDropSearch();
    if (this.user.userId) {
      this.walletService.getWalletByUserId(this.user.userId)
        .pipe(catchError(() => of({ balance: 0 })))
        .subscribe((w: any) => { this.walletBalance = w?.balance ?? 0; });
    }
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  /* ── Close dropdowns on outside click ── */
  @HostListener('document:click', ['$event'])
  onDocClick(e: MouseEvent): void {
    if (!this.elRef.nativeElement.contains(e.target)) {
      this.showPickupDrop = false;
      this.showDropDrop   = false;
    }
  }

  /* ── History helpers ── */
  private loadHistory(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    try {
      const raw = localStorage.getItem(HISTORY_KEY);
      this.history = raw ? JSON.parse(raw) : [];
    } catch { this.history = []; }
  }

  private saveToHistory(addr: string, lat?: number, lng?: number): void {
    if (!isPlatformBrowser(this.platformId) || !addr.trim()) return;
    this.history = this.history.filter(h => h.address.toLowerCase() !== addr.toLowerCase());
    this.history.unshift({ address: addr, lat, lng, type: 'history' });
    if (this.history.length > MAX_HISTORY) this.history.length = MAX_HISTORY;
    try { localStorage.setItem(HISTORY_KEY, JSON.stringify(this.history)); } catch {}
  }

  private filterHistory(query: string): LocationSuggestion[] {
    const q = query.toLowerCase();
    return this.history
      .filter(h => h.address.toLowerCase().includes(q))
      .slice(0, 3)
      .map(h => ({ ...h, type: 'history' as const }));
  }

  /* ── Nominatim search ── */
  private nominatimSearch(query: string) {
    const url = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(query)}&format=json&limit=4&countrycodes=in&addressdetails=0`;
    return this.http.get<any[]>(url, {
      headers: { 'Accept-Language': 'en', 'User-Agent': 'CarryGoApp/1.0' }
    }).pipe(catchError(() => of([])));
  }

  private nominatimToSuggestions(results: any[]): LocationSuggestion[] {
    return results.map(r => ({
      address: r.display_name as string,
      lat: parseFloat(r.lat),
      lng: parseFloat(r.lon),
      type: 'nominatim' as const,
    }));
  }

  /* ── Pickup autocomplete ── */
  private setupPickupSearch(): void {
    const sub = this.pickupSearch$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap(query => {
        if (query.length < 2) { this.pickupLoading = false; return of([]); }
        this.pickupLoading = true;
        return this.nominatimSearch(query);
      }),
    ).subscribe(results => {
      this.pickupLoading = false;
      const live = this.nominatimToSuggestions(results as any[]);
      const hist = this.filterHistory(this.pickupLocation);
      this.pickupSuggestions = [...hist, ...live.filter(l =>
        !hist.some(h => h.address.toLowerCase() === l.address.toLowerCase()))];
      this.showPickupDrop = this.pickupSuggestions.length > 0;
      this.cdr.detectChanges();
    });
    this.subs.add(sub);
  }

  onPickupInput(): void {
    this.searched = false;
    this.pickupLat = undefined;
    this.pickupLng = undefined;
     this.distanceKm = 0;
      this.totalPrice = 0;
    const q = this.pickupLocation.trim();
    if (!q) {
      this.pickupSuggestions = this.history.slice(0, 5).map(h => ({ ...h, type: 'history' as const }));
      this.showPickupDrop = this.pickupSuggestions.length > 0;
      return;
    }
    this.pickupSearch$.next(q);
  }

  onPickupFocus(): void {
    if (!this.pickupLocation.trim()) {
      this.pickupSuggestions = this.history.slice(0, 5).map(h => ({ ...h, type: 'history' as const }));
      this.showPickupDrop = this.pickupSuggestions.length > 0;
    } else {
      this.showPickupDrop = this.pickupSuggestions.length > 0;
    }
  }

  selectPickup(s: LocationSuggestion): void {
    this.pickupLocation = s.address;
    this.pickupLat      = s.lat;
    this.pickupLng      = s.lng;
    this.showPickupDrop = false;
    this.pickupSuggestions = [];
     this.tryFetchFare();
  }

  /* ── Drop autocomplete ── */
  private setupDropSearch(): void {
    const sub = this.dropSearch$.pipe(
      debounceTime(350),
      distinctUntilChanged(),
      switchMap(query => {
        if (query.length < 2) { this.dropLoading = false; return of([]); }
        this.dropLoading = true;
        return this.nominatimSearch(query);
      }),
    ).subscribe(results => {
      this.dropLoading = false;
      const live = this.nominatimToSuggestions(results as any[]);
      const hist = this.filterHistory(this.dropLocation);
      this.dropSuggestions = [...hist, ...live.filter(l =>
        !hist.some(h => h.address.toLowerCase() === l.address.toLowerCase()))];
      this.showDropDrop = this.dropSuggestions.length > 0;
      this.cdr.detectChanges();
    });
    this.subs.add(sub);
  }

  onDropInput(): void {
    this.searched = false;
    this.dropLat = undefined;
    this.dropLng = undefined;
    this.distanceKm = 0;
      this.totalPrice = 0;
    const q = this.dropLocation.trim();
    if (!q) {
      this.dropSuggestions = this.history.slice(0, 5).map(h => ({ ...h, type: 'history' as const }));
      this.showDropDrop = this.dropSuggestions.length > 0;
      return;
    }
    this.dropSearch$.next(q);
  }

  onDropFocus(): void {
    if (!this.dropLocation.trim()) {
      this.dropSuggestions = this.history.slice(0, 5).map(h => ({ ...h, type: 'history' as const }));
      this.showDropDrop = this.dropSuggestions.length > 0;
    } else {
      this.showDropDrop = this.dropSuggestions.length > 0;
    }
  }

  selectDrop(s: LocationSuggestion): void {
    this.dropLocation = s.address;
    this.dropLat      = s.lat;
    this.dropLng      = s.lng;
    this.showDropDrop = false;
    this.dropSuggestions = [];
     this.tryFetchFare();
  }

  /* ── Map picker ── */
  openMap()  { this.showMapPicker = true; }
  closeMap() { this.showMapPicker = false; }

  onMapConfirm(r: MapPickerResult): void {
    this.showMapPicker  = false;
    this.pickupLocation = r.pickupAddress;
    this.pickupLat      = r.pickupLat;
    this.pickupLng      = r.pickupLng;
    this.dropLocation   = r.dropAddress;
    this.dropLat        = r.dropLat;
    this.dropLng        = r.dropLng;
    this.distanceKm     = r.distanceKm;
    this.recalcPrice();
    this.cdr.detectChanges();
  }

  /* ── Weight slider ── */
  get weightDisplay(): string {
    return this.weightKg < 1 ? `${this.weightKg * 1000}g` : `${this.weightKg} kg`;
  }

  get sliderPct(): number {
    return ((this.weightKg - 0.5) / (30 - 0.5)) * 100;
  }

  /* ── Price calc ── */
  recalcPrice(): void {
    if (!this.distanceKm) {
      return;
    }
    const express     = this.deliveryType === 'express' ? 1.5 : 1;
    this.basePrice    = Math.round(40 * express);
    this.distanceCost = Math.round(this.distanceKm * 8 * Math.max(1, this.weightKg) * express);
    this.serviceFee   = Math.round(15 * express);
    this.totalPrice   = this.basePrice + this.distanceCost + this.serviceFee;
  }
  /**
   * Queries the backend for real matching online porters.
   * Falls back to 0 if the API is unreachable.
   */
  findCommuters(): void {
    if (!this.pickupLocation.trim() || !this.dropLocation.trim()) return;
    this.showPickupDrop    = false;
    this.showDropDrop      = false;
    this.recalcPrice();
    this.searched          = false;
    this.isLoadingCommuters = true;
    this.cdr.detectChanges();
  this.tryFetchFare();
    this.deliveryService.getMatchingPortersCount(
      this.pickupLat, this.pickupLng, this.dropLat, this.dropLng
    ).pipe(catchError(() => of(0))).subscribe(count => {
      this.commuterCount     = count as number;
      this.searched          = true;
      this.isLoadingCommuters = false;
      this.cdr.detectChanges();
    });
  }

  /* ── Book delivery ── */
  bookDelivery(): void {
    if (this.isSubmitting || !this.user.userId) return;

    if (this.walletBalance < this.totalPrice) {
      this.walletError = `Insufficient wallet balance. Please add money to your wallet to place this order.`;
      this.cdr.detectChanges();
      return;
    }
    this.walletError = '';
    this.isSubmitting = true;

    this.saveToHistory(this.pickupLocation, this.pickupLat, this.pickupLng);
    this.saveToHistory(this.dropLocation,   this.dropLat,   this.dropLng);

    const payload = {
      senderId:            this.user.userId,
      pickupAddress:       this.pickupLocation,
      pickupLat:           this.pickupLat    ?? null,
      pickupLng:           this.pickupLng    ?? null,
      pickupContact:       this.contactName  || null,
      pickupPhone:         this.contactPhone || null,
      dropAddress:         this.dropLocation,
      dropLat:             this.dropLat      ?? null,
      dropLng:             this.dropLng      ?? null,
      receiverName:        this.receiverName,
      receiverPhone:       this.receiverPhone,
      packageType:         this.packageType  || 'other',
      packageSize:         this.packageSize,
      weightKg:            Number(this.weightKg),
      specialInstructions: this.specialInstructions,
      deliveryType:        this.deliveryType.toUpperCase(),
      preferredDate:       this.preferredDate   || null,
      preferredTime:       this.preferredTime   || null,
      distanceKm:          this.distanceKm,
      basePrice:           this.basePrice,
      distanceCost:        this.distanceCost,
      serviceFee:          this.serviceFee,
      totalAmount:         this.totalPrice,
      status:              'PENDING',
    };

    this.deliveryService.createDelivery(payload).subscribe({
      next: () => {
        this.router.navigate(['/user-dashboard', this.user.userId]);
      },
      error: err => {
        console.error('Book error', err);
        alert('Failed to book. Please try again.');
        this.isSubmitting = false;
        this.cdr.detectChanges();
      },
    });
  }

private tryFetchFare(): void {
    if (!this.pickupLat || !this.pickupLng || !this.dropLat || !this.dropLng) return; // wait for both
  this.fareService.estimate(
    this.pickupLat, this.pickupLng,
    this.dropLat,   this.dropLng
  ).subscribe(res => {
    this.distanceKm   = res.distanceKm;
    this.basePrice    = res.baseFare;
    this.distanceCost = res.distanceFare;
    this.serviceFee   = res.zoneSurcharge;
    this.totalPrice   = res.totalFare;
    this.cdr.detectChanges();
  });
}
  get canSearch(): boolean {
    return this.pickupLocation.trim().length > 0 && this.dropLocation.trim().length > 0;
  }

  getEta(): string {
    return this.deliveryType === 'express' ? '30–90 mins' : '2–4 hours';
  }

  /* ── Reset entire form ── */
  resetForm(): void {
    this.pickupLocation = '';
    this.pickupLat = undefined;
    this.pickupLng = undefined;
    this.contactName = '';
    this.contactPhone = '';

    this.dropLocation = '';
    this.dropLat = undefined;
    this.dropLng = undefined;
    this.receiverName = '';
    this.receiverPhone = '';

    this.packageType = '';
    this.weightKg = 1;
    this.packageSize = 'medium';
    this.specialInstructions = '';

    this.deliveryType = 'standard';
    this.preferredDate = '';
    this.preferredTime = '';
    this.flexibleMatching = true;

    this.distanceKm = 0;
    this.basePrice = 0;
    this.distanceCost = 0;
    this.serviceFee = 0;
    this.totalPrice = 0;

    this.walletError = '';
    this.searched = false;
    this.commuterCount = 0;

    this.pickupSuggestions = [];
    this.dropSuggestions = [];
    this.showPickupDrop = false;
    this.showDropDrop = false;

    this.cdr.detectChanges();
  }
}
