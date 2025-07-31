import { Component, OnDestroy, OnInit } from '@angular/core';
import { FileRequestService } from '../services/file-request-service';
import { FileRequest } from '../model/FileRequest';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FileSizePipe } from '../pipes/file-size-pipe';
import { trigger, transition, style, animate } from '@angular/animations';

@Component({
  selector: 'app-host-component',
  imports: [MatProgressSpinnerModule, FileSizePipe],
  templateUrl: './host-component.html',
  styleUrl: './host-component.scss',
  animations: [
    trigger('listAnimation', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-20px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ opacity: 0, transform: 'translateY(20px)' }))
      ])
    ])
  ]
})
export class HostComponent implements OnInit, OnDestroy {
  pendingRequests: FileRequest[] = [];

  constructor(
    private fileRequestService: FileRequestService,
  ) {}

  async ngOnInit() {
    this.fileRequestService.connectAsHost();
    this.pendingRequests = await this.fileRequestService.getPendingRequests()
    this.listenForRequests();
  }

  ngOnDestroy() {
    this.fileRequestService.disconnect();
  }

  private listenForRequests() {
    this.fileRequestService.onRequestReceived().subscribe((request: FileRequest) => {
      this.pendingRequests.push(request);
    });
  }

  approveRequest(request: FileRequest) {
    this.fileRequestService.sendResponse(request.requestId, true);
    this.removeRequest(request.requestId);
  }

  rejectRequest(request: FileRequest) {
    this.fileRequestService.sendResponse(request.requestId, false);
    this.removeRequest(request.requestId);
  }

  private removeRequest(requestId: string) {
    this.pendingRequests = this.pendingRequests.filter(req => req.requestId !== requestId);
  }
}