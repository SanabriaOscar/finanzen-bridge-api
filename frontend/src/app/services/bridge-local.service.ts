import { Injectable, OnDestroy } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { BRIDGE_EVENT_TYPES } from '../constants/bridge-events.constants';
import { BRIDGE_LOCAL } from '../constants/bridge-local.constants';

export interface BridgeEnvelope {
  schemaVersion: number;
  type: string;
  payload?: Record<string, unknown>;
  timestamp?: number;
}

@Injectable({ providedIn: 'root' })
export class BridgeLocalService implements OnDestroy {
  private socket: WebSocket | null = null;
  private readonly connected$ = new BehaviorSubject<boolean>(false);
  readonly connectionState$ = this.connected$.asObservable();
  readonly messages$ = new Subject<BridgeEnvelope>();

  connect(token: string = BRIDGE_LOCAL.DEFAULT_PAIRING_TOKEN): void {
    this.disconnect();
    const url = `ws://${BRIDGE_LOCAL.DEFAULT_HOST}:${BRIDGE_LOCAL.DEFAULT_PORT}${BRIDGE_LOCAL.WS_PATH}?token=${encodeURIComponent(token)}`;
    this.socket = new WebSocket(url);
    this.socket.onopen = () => this.connected$.next(true);
    this.socket.onclose = () => this.connected$.next(false);
    this.socket.onerror = () => this.connected$.next(false);
    this.socket.onmessage = (ev) => {
      try {
        const data = JSON.parse(String(ev.data)) as BridgeEnvelope;
        this.messages$.next(data);
      } catch {
        // ignore malformed
      }
    };
  }

  ping(): void {
    this.send({ type: BRIDGE_EVENT_TYPES.PING });
  }

  testPrinter(): void {
    this.send({ type: BRIDGE_EVENT_TYPES.TEST_PRINTER });
  }

  requestWeight(): void {
    this.send({ type: BRIDGE_EVENT_TYPES.REQUEST_WEIGHT });
  }

  send(payload: Record<string, unknown>): void {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
      return;
    }
    this.socket.send(JSON.stringify({ schemaVersion: BRIDGE_LOCAL.SCHEMA_VERSION, ...payload }));
  }

  disconnect(): void {
    this.socket?.close();
    this.socket = null;
    this.connected$.next(false);
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.messages$.complete();
  }
}
