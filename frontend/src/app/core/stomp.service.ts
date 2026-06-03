import { Injectable, signal } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject } from 'rxjs';

import { AlertView, DeviceView } from './models';

@Injectable({ providedIn: 'root' })
export class StompService {
  private readonly client: Client;
  private readonly deviceSubject = new Subject<DeviceView>();
  private readonly alertSubject = new Subject<AlertView>();
  private readonly connectedState = signal(false);

  readonly devices$ = this.deviceSubject.asObservable();
  readonly alerts$ = this.alertSubject.asObservable();
  readonly connected = this.connectedState.asReadonly();

  constructor() {
    this.client = new Client({
      brokerURL: this.brokerUrl(),
      reconnectDelay: 5000,
      onConnect: () => {
        this.connectedState.set(true);
        this.client.subscribe('/topic/devices', (message) =>
          this.emit(message, this.deviceSubject),
        );
        this.client.subscribe('/topic/alerts', (message) => this.emit(message, this.alertSubject));
      },
      onWebSocketClose: () => this.connectedState.set(false),
      onStompError: (frame) => {
        console.error('STOMP error', frame.headers['message'], frame.body);
      },
    });
  }

  activate(): void {
    if (!this.client.active) {
      this.client.activate();
    }
  }

  private emit<T>(message: IMessage, target: Subject<T>): void {
    target.next(JSON.parse(message.body) as T);
  }

  private brokerUrl(): string {
    const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
    return `${protocol}://${location.host}/ws`;
  }
}
