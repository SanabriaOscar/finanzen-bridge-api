import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { BridgeLocalService, BridgeEnvelope } from './services/bridge-local.service';
import { BRIDGE_EVENT_TYPES } from './constants/bridge-events.constants';
import { BRIDGE_LOCAL } from './constants/bridge-local.constants';

@Component({
  selector: 'bridge-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css',
})
export class AppComponent implements OnInit, OnDestroy {
  @ViewChild('scannerInput') scannerInput?: ElementRef<HTMLInputElement>;

  connected = false;
  lastEvent = '';
  clients = 0;
  scaleAvailable = false;
  printerAvailable = false;
  lastScan = '';
  scannerBuffer = '';
  logs: string[] = [];
  private subs = new Subscription();

  constructor(private readonly bridge: BridgeLocalService) {}

  ngOnInit(): void {
    this.bridge.connect();
    this.subs.add(this.bridge.connectionState$.subscribe((v) => (this.connected = v)));
    this.subs.add(this.bridge.messages$.subscribe((msg) => this.onMessage(msg)));
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.bridge.disconnect();
  }

  ping(): void { this.bridge.ping(); }
  testPrinter(): void { this.bridge.testPrinter(); }
  requestWeight(): void { this.bridge.requestWeight(); }

  focusScanner(): void {
    this.scannerInput?.nativeElement.focus();
  }

  clearScan(): void {
    this.scannerBuffer = '';
    this.lastScan = '';
  }

  onScannerEnter(event: Event): void {
    event.preventDefault();
    const code = this.scannerBuffer.trim();
    if (!code) {
      return;
    }
    this.lastScan = code;
    this.bridge.send({
      type: BRIDGE_EVENT_TYPES.SCANNER_INPUT,
      payload: { code, source: 'HID' },
    });
    this.scannerBuffer = '';
    this.focusScanner();
  }

  private pushLog(type: string, payload: Record<string, unknown>): void {
    const line = `[${type}] ${JSON.stringify(payload)}`;
    this.logs = [line, ...this.logs].slice(0, 30);
    this.lastEvent = type;
  }

  private onMessage(msg: BridgeEnvelope): void {
    const line = `[${msg.type}] ${JSON.stringify(msg.payload ?? {})}`;
    this.logs = [line, ...this.logs].slice(0, 30);
    this.lastEvent = msg.type ?? '';
    if (msg.type === BRIDGE_EVENT_TYPES.SCANNER_INPUT && msg.payload?.['code']) {
      this.lastScan = String(msg.payload['code']);
    }
    if (msg.type === BRIDGE_EVENT_TYPES.BRIDGE_READY && msg.payload) {
      this.clients = Number(msg.payload['clients'] ?? 0);
      this.scaleAvailable = Boolean(msg.payload['scaleAvailable']);
      this.printerAvailable = Boolean(msg.payload['printerAvailable']);
    }
  }

  readonly appName = 'Finnazen Bridge';
  readonly wsUrl = `ws://${BRIDGE_LOCAL.DEFAULT_HOST}:${BRIDGE_LOCAL.DEFAULT_PORT}${BRIDGE_LOCAL.WS_PATH}`;
}
