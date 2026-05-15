import {
  Component, OnInit, AfterViewChecked, ViewChild,
  ElementRef, ChangeDetectorRef, HostListener
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../services/auth.service';

interface Message { from: 'bot' | 'user'; text: string; time: string; }
interface QuickReply { label: string; action: string; }

/* ══════════════════════════════════════════════════════════
   🔑 GEMINI API KEY — paste yours between the quotes.
   Get one free at https://aistudio.google.com/app/apikey
   ══════════════════════════════════════════════════════════ */
const GEMINI_API_KEY = 'AIzaSyAgMocqoTnnvWWEhAgQ6Z12hTvlGpjS_dY';
const GEMINI_MODEL   = 'gemini-2.5-flash-lite';
const GEMINI_URL     = `https://generativelanguage.googleapis.com/v1beta/models/${GEMINI_MODEL}:streamGenerateContent`;

const UI = {
  placeholder: 'Ask me anything about CarryGo…',
  title:       'CarryBot',
  subtitle:    'Your AI delivery assistant',
};

const GREETING =
  "Hi there! 👋 I'm CarryBot, your AI assistant for CarryGo.\n\n" +
  "I can help you send a parcel, track a delivery, manage your wallet, complete KYC, " +
  "or answer any question about how the app works.\n\nWhat would you like to do?";

const QUICK_REPLIES: QuickReply[] = [
  { label: '📦 Send a Parcel',  action: 'How do I send a parcel?' },
  { label: '📋 Track Delivery', action: 'How do I track my deliveries?' },
  { label: '💳 Wallet',         action: 'How does the CarryGo wallet work?' },
  { label: '🛡️ KYC',            action: 'How does porter KYC verification work?' },
  { label: '💰 Pricing',        action: 'How is the fare calculated?' },
  { label: '❓ Help',           action: 'What can you help me with?' },
];

const OFFLINE_REPLY =
  "I'm having trouble reaching the AI right now 🤕\n\nPlease try again in a moment.";

/* ── Maps the current route → short page description for the AI ── */
function describePage(url: string): string {
  if (url.includes('/login'))             return 'Login page';
  if (url.includes('/commuter-register')) return 'Commuter opt-in page';
  if (url.includes('/register'))          return 'Sign-up / Registration page';
  if (url.includes('/send-parcel'))       return 'Send Parcel page (standalone)';
  if (url.includes('/user-dashboard'))    return 'User Dashboard — sidebar: Home (booking form), My Deliveries, Wallet, Services/Intercity';
  if (url.includes('/porter-dashboard'))  return 'Porter Dashboard — incoming requests, earnings, online/offline toggle';
  if (url.includes('/porter-deliveries')) return 'Porter Deliveries page';
  if (url.includes('/porter-kyc'))        return 'Porter KYC — 6-step form with Reset / Reset All buttons';
  if (url.includes('/porter-profile'))    return 'Porter Profile page';
  return 'CarryGo app';
}

/* ── System prompt sent to Gemini on every request ── */
function buildSystemPrompt(page: string, userName: string, userRole: string): string {
  const who = userName
    ? `The user is logged in as "${userName}"${userRole ? ` (role: ${userRole})` : ''}.`
    : `The user is not logged in.`;

  return `You are CarryBot, the friendly AI assistant inside CarryGo — a peer-to-peer parcel delivery app where everyday commuters (called "porters") carry parcels along their existing routes.

${who}
Right now the user is on: ${page}.

═══ APP KNOWLEDGE ═══

ROLES
• "user" / sender — books parcels, pays from wallet, tracks deliveries.
• "porter" / "commuter" — carries parcels; must complete KYC first; earns ~85% of fare (15% platform commission).

USER DASHBOARD (sidebar)
1. Home — the live parcel booking form (pickup, drop, vehicle, package, get fare, book). Has a Reset Form button.
2. My Deliveries — every order with status PENDING / ACCEPTED / PICKED_UP / DELIVERED / CANCELLED.
3. Wallet — balance, Add Money (UPI / Card), full transaction history. Cancelled bookings auto-refund here.
4. Services / Intercity — extra intercity courier listings.

PORTER DASHBOARD (sidebar)
1. Dashboard, 2. Deliveries, 3. KYC (6 steps with Reset / Reset All), 4. Profile.
Header has Online/Offline toggle — porter MUST be Online to get job notifications.

BOOKING FLOW
1. Dashboard → Home.
2. Pickup + drop addresses (Nominatim autocomplete or map picker).
3. Vehicle: bike / auto / mini / sedan / suv (auto is default).
4. Package type, weight, receiver name + phone.
5. "Get Price Estimate" → fare card.
6. "Book Delivery — ₹X" → debits wallet, broadcasts to nearby online porters.
7. While waiting, user can Cancel Booking (auto-refund).

REGISTRATION
• /register, "Register as User" or "Register as Commuter".
• Full name (≤20 chars), Gmail only, country code + 10-digit phone, password (8+ chars, 1 upper, 1 number, 1 special).
• Has a Reset button. Commuters then finish KYC.

═══ PRICING — THE REAL LIVE FORMULA ═══
(Exact backend logic. Quote these numbers. Do NOT describe any other model — no Standard/Express, no weight-based pricing.)

VEHICLE RATES [base fare, ₹/km, ₹/min]
• bike  — ₹15, ₹8/km, ₹0.5/min
• auto  — ₹25, ₹12/km, ₹1.0/min  (default)
• mini  — ₹40, ₹14/km, ₹1.5/min
• sedan — ₹60, ₹18/km, ₹2.0/min
• suv   — ₹80, ₹22/km, ₹2.5/min

DISTANCE & TIME
• Distance = haversine km between pickup/drop.
• Minutes = max(5, distance / 30 km/h × 60).

TIME-OF-DAY MULTIPLIER
• 08:00–10:00 and 18:00–21:00 → 1.3× (peak)
• 23:00–05:00 → 1.2× (night)
• Otherwise → 1.0×

DEMAND SURGE (active PENDING ÷ online porters)
• 0 porters → 2.0×
• <1.2 → 1.0× Normal · <1.5 → 1.2× Slight · <2.0 → 1.5× High · <3.0 → 1.8× Very High · ≥3.0 → 2.0× Surge

ZONE SURCHARGE
• Bangalore airport +₹80 · Delhi/Mumbai/Hyderabad airport +₹60 · Chennai/Kolkata airport +₹50 · Pune airport +₹30 · else +₹0

TOTAL
subtotal = (baseFare + distanceFare + timeFare) × (time-of-day × surge)
total = round((subtotal + zoneSurcharge) × 10) / 10
fareRange = total ± 5%

═══ KYC — 6 STEPS ═══
1. Personal (name, DOB 18+, gender, 10-digit phone, valid email)
2. Identity (Aadhaar / PAN / Voter / Passport / DL + front image, JPG/PNG/PDF, max 5MB)
3. Address (house, street, city, state, 6-digit PIN)
4. Vehicle (type, model, reg no., licence no. + expiry)
5. Bank (holder, account + confirm, IFSC matching ^[A-Z]{4}0[A-Z0-9]{6}$, bank name)
6. Legal (scroll terms, tick both checkboxes, submit)

═══ STYLE ═══
• Reply in ENGLISH only.
• Greetings / thanks / bye / small talk → warm 1–2 sentence reply, don't dump knowledge base.
• "How do I…" questions → numbered steps.
• Use a few relevant emojis (📦 📋 💳 🛡️ ✅ 👋) — don't overdo it.
• Use \\n line breaks; keep paragraphs short.
• Reference the user's current page when relevant.
• NEVER invent order IDs, prices, ETAs, porter names, account numbers — point to where to look in the app.
• When asked pricing, quote the EXACT formula above.
• Off-topic general questions (jokes, math, casual) → brief friendly answer, then steer back if needed.`;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.html',
  styleUrl: './chatbot.css',
})
export class ChatbotComponent implements OnInit, AfterViewChecked {

  @ViewChild('chatBody') chatBody!: ElementRef<HTMLDivElement>;

  isOpen    = false;
  isTyping  = false;
  inputText = '';
  unread    = 1;
  messages: Message[] = [];

  ui           = UI;
  quickReplies = QUICK_REPLIES;

  private currentUrl = '/';
  private userName   = '';
  private userRole   = '';

  constructor(
    private cdr: ChangeDetectorRef,
    private router: Router,
    private authService: AuthService,
  ) {}

  ngOnInit(): void {
    this.currentUrl = this.router.url;
    this.router.events
      .pipe(filter(e => e instanceof NavigationEnd))
      .subscribe((e: any) => { this.currentUrl = e.urlAfterRedirects || e.url; });

    this.authService.currentUser$.subscribe(u => {
      this.userName = u?.name || '';
      this.userRole = u?.role || '';
    });

    setTimeout(() => this.pushBot(GREETING), 600);
  }

  ngAfterViewChecked(): void { this.scrollBottom(); }

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) this.unread = 0;
  }

  @HostListener('document:keydown.escape')
  onEsc(): void { this.isOpen = false; }

  clearChat(): void {
    this.messages = [];
    setTimeout(() => this.pushBot(GREETING), 200);
  }

  send(): void {
    const text = this.inputText.trim();
    if (!text) return;
    this.inputText = '';
    this.pushUser(text);
    this.respondWithAI(text);
  }

  onQuickReply(action: string): void {
    this.pushUser(action);
    this.respondWithAI(action);
  }

  /* ── Stream a reply from Gemini and render tokens as they arrive ── */
  private async respondWithAI(userText: string): Promise<void> {
    this.isTyping = true;
    this.cdr.detectChanges();

    // History — Gemini requires `contents` to start with a `user` turn.
    let history = this.messages.slice(0, -1).slice(-12).map(m => ({
      role:  m.from === 'user' ? 'user' : 'model',
      parts: [{ text: m.text }],
    }));
    while (history.length && history[0].role !== 'user') history.shift();
    const body = {
      systemInstruction: { parts: [{ text: buildSystemPrompt(describePage(this.currentUrl), this.userName, this.userRole) }] },
      contents: [...history, { role: 'user', parts: [{ text: userText }] }],
      generationConfig: { temperature: 0.7, maxOutputTokens: 600 },
    };

    const url = `${GEMINI_URL}?alt=sse&key=${encodeURIComponent(GEMINI_API_KEY)}`;
    let liveIdx = -1;

    try {
      const res = await fetch(url, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(body),
      });

      if (!res.ok || !res.body) throw new Error(`HTTP ${res.status}`);

      const reader  = res.body.getReader();
      const decoder = new TextDecoder();
      let buffer    = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });

        let nl: number;
        while ((nl = buffer.indexOf('\n')) !== -1) {
          const line = buffer.slice(0, nl).trim();
          buffer = buffer.slice(nl + 1);
          if (!line.startsWith('data:')) continue;
          const json = line.slice(5).trim();
          if (!json || json === '[DONE]') continue;

          try {
            const piece = JSON.parse(json)?.candidates?.[0]?.content?.parts
              ?.map((p: any) => p.text).join('') || '';
            if (!piece) continue;

            if (liveIdx === -1) {
              this.isTyping = false;
              this.messages.push({ from: 'bot', text: '', time: this.now() });
              if (!this.isOpen) this.unread++;
              liveIdx = this.messages.length - 1;
            }
            this.messages[liveIdx].text += piece;
            this.cdr.detectChanges();
          } catch { /* ignore malformed chunk */ }
        }
      }

      if (liveIdx === -1) {
        this.isTyping = false;
        this.pushBot(OFFLINE_REPLY);
      }
    } catch (err) {
      console.error('Gemini error:', err);
      this.isTyping = false;
      if (liveIdx === -1) this.pushBot(OFFLINE_REPLY);
    }
  }

  /* ── Helpers ── */
  private pushBot(text: string): void {
    this.messages.push({ from: 'bot', text, time: this.now() });
    if (!this.isOpen) this.unread++;
    this.cdr.detectChanges();
  }

  private pushUser(text: string): void {
    this.messages.push({ from: 'user', text, time: this.now() });
    this.cdr.detectChanges();
  }

  private now(): string {
    return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private scrollBottom(): void {
    const el = this.chatBody?.nativeElement;
    if (el) el.scrollTop = el.scrollHeight;
  }

  formatText(text: string): string { return text.replace(/\n/g, '<br>'); }
}
