import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, StompSubscription } from '@stomp/stompjs';

// Shape of every real-time message the backend sends us over WebSocket.
export interface SseEvent {
  event: string;
  data: any;
}

// Single shared WebSocket connection for all real-time updates:
// new ride requests, delivery status changes, and chat messages.
// The class is named "SseService" for legacy reasons but actually uses STOMP over WebSocket.
@Injectable({ providedIn: 'root' })
export class SseService {
  private stompClient: Client | null = null;

  // All incoming events are pushed onto this Subject; components subscribe to it via connect().
  private subject = new Subject<SseEvent>();

  // Chat room subscriptions, keyed by deliveryId — so we can unsubscribe when the chat closes.
  private chatSubs = new Map<number, StompSubscription>();

  // Chat rooms requested before the WebSocket finished opening.
  // We re-apply them once onConnect fires so no subscription is lost.
  private pendingChatSubs = new Set<number>();

  private readonly wsUrl = 'ws://localhost:8081/ws';

  // Open the WebSocket. Call this once after login.
  // Pass isPorter=true to also listen for new ride requests broadcast to all porters.
  connect(userId: number, isPorter = false): Observable<SseEvent> {
    this.disconnect();

    this.stompClient = new Client({
      brokerURL: this.wsUrl,
      reconnectDelay: 5000,

      onConnect: () => {
        // Personal channel — direct events for this user (status updates, chat, etc.).
        this.stompClient!.subscribe(`/topic/user/${userId}`, msg => this.emit(msg.body));

        // Porters also listen for new ride requests broadcast to everyone.
        if (isPorter) {
          this.stompClient!.subscribe('/topic/new-orders', msg => this.emit(msg.body));
        }

        // Apply chat subscriptions that were queued while we were still connecting.
        this.pendingChatSubs.forEach(id => this.doSubscribeToChat(id));
        this.pendingChatSubs.clear();
      },
    });

    this.stompClient.activate();
    return this.subject.asObservable();
  }

  // Listen for chat messages on a specific delivery.
  // Safe to call before connect() finishes — the subscription is queued and applied on connect.
  subscribeToChat(deliveryId: number): void {
    if (this.chatSubs.has(deliveryId)) return;

    if (!this.stompClient?.connected) {
      this.pendingChatSubs.add(deliveryId);
      return;
    }

    this.doSubscribeToChat(deliveryId);
  }

  // Stop listening to a chat room (called when the chat window closes).
  unsubscribeFromChat(deliveryId: number): void {
    this.pendingChatSubs.delete(deliveryId);
    const sub = this.chatSubs.get(deliveryId);
    if (sub) { sub.unsubscribe(); this.chatSubs.delete(deliveryId); }
  }

  // Close everything and reset state. Called on logout or when reconnecting.
  disconnect(): void {
    this.chatSubs.forEach(s => s.unsubscribe());
    this.chatSubs.clear();
    this.pendingChatSubs.clear();
    this.stompClient?.deactivate();
    this.stompClient = null;
  }

  // Actually opens the chat subscription. Only call when the WebSocket is connected.
  private doSubscribeToChat(deliveryId: number): void {
    if (this.chatSubs.has(deliveryId) || !this.stompClient?.connected) return;
    const sub = this.stompClient.subscribe(
      `/topic/chat/${deliveryId}`,
      msg => this.emit(msg.body)
    );
    this.chatSubs.set(deliveryId, sub);
  }

  // Parses the JSON body the server sent and forwards it to subscribers.
  private emit(body: string): void {
    try {
      const parsed = JSON.parse(body) as SseEvent;
      this.subject.next(parsed);
    } catch {}
  }
}
