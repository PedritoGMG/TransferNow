import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import { Subject } from 'rxjs/internal/Subject';
import SockJS from 'sockjs-client';
import { fileTypeFromBlob } from 'file-type';
import * as hashjs from 'hash.js';
import { AxiosService } from './axios.service';
import { FileRequest } from '../model/FileRequest';

@Injectable({
  providedIn: 'root'  
})
export class FileRequestService {
  private stompClient!: Client;
  private requestSubject = new Subject<any>();
  private currentRequestId: string | null = null;
  private approvalSubject = new Subject<string>();
  private rejectionSubject = new Subject<void>();
  private isHost: boolean = false;
  private sessionId: string = '';

  constructor(public AxiosService:AxiosService) { }

  private generateSessionId(): string {
    let sessionId = sessionStorage.getItem('webSocketSessionId');
    if (!sessionId) {
      sessionId = 'session-' + Math.random().toString(36).substring(2) + Date.now().toString(36);
      sessionStorage.setItem('webSocketSessionId', sessionId);
    }
    return sessionId;
  }

  public async getPendingRequests(): Promise<FileRequest[]> {
    return this.AxiosService.get(`/getRequests`);
  }

  getSessionId(): string {
    return this.sessionId;
  }

  connectAsHost() {
    this.isHost = true;
    this.connect();
  }

  connectAsGuest() {
    this.isHost = false;
    this.connect();
  }

  private connect() {
    const socket = new SockJS('/ws');
    this.stompClient = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: (frame) => {
        this.extractRealSessionId(frame);
        
        if (this.isHost) {
          this.stompClient.subscribe('/queue/host-requests', (message) => {
            this.requestSubject.next(JSON.parse(message.body));
          });
        } else {
          this.subscribeToGuestChannels();
        }
      }
    });
    this.stompClient.activate();
  }
  private extractRealSessionId(frame: any) {
    const sessionId = frame.headers['user-name'] || 
                    frame.headers['simpSessionId'] ||
                    frame.headers['session-id'];
    
    if (sessionId) {
      this.sessionId = sessionId.replace('user-', '');
      sessionStorage.setItem('webSocketSessionId', this.sessionId);
    } else {
      this.sessionId = this.generateSessionId();
    }
  }

  private subscribeToGuestChannels() {
    this.stompClient.subscribe(`/user/queue/upload-token`, (message) => {
      this.approvalSubject.next(message.body);
    });

    this.stompClient.subscribe(`/user/queue/rejected`, (message) => {
      this.rejectionSubject.next();
    });
  }


  async sendFileRequest(file: File, senderName: string): Promise<string> {
    this.currentRequestId = crypto.randomUUID().toString();
    
    const [fileHash, realMimeType] = await Promise.all([
        this.calculateFileHash(file),
        this.detectRealMimeType(file)
    ]);

    this.stompClient.publish({
        destination: '/app/request/initiate',
        body: JSON.stringify({
            requestId: this.currentRequestId,
            fileName: file.name,
            fileType: file.type,
            mimeType: realMimeType,
            fileSize: file.size,
            fileHash: fileHash,
            senderName: senderName,
            senderSessionId: this.sessionId
        })
    });
    return this.currentRequestId;
  }

  private async calculateFileHash(file: File): Promise<string> {
    const buffer = await file.arrayBuffer();
    return hashjs.sha256().update(new Uint8Array(buffer)).digest('hex');
  }
  
  private async detectRealMimeType(file: File): Promise<string> {
    try {
        const type = await fileTypeFromBlob(file);
        return type?.mime || file.type || 'application/octet-stream';
    } catch {
        return file.type || 'application/octet-stream';
    }
  }

  listenForApproval(callback: (token: string) => void) {
    this.approvalSubject.subscribe(callback);
  }
  
  listenForRejection(callback: () => void) {
    this.rejectionSubject.subscribe(callback);
  }

  sendResponse(requestId: string, approved: boolean) {
    this.stompClient.publish({
      destination: '/app/request/respond',
      headers: { 'requestId': requestId },
      body: approved ? "ACCEPT" : "REJECT"
    });
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.deactivate();
    }
  }

  onRequestReceived() {
    return this.requestSubject.asObservable();
  }
}
