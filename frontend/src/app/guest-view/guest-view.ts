import { Component, OnDestroy } from '@angular/core';
import { FileRequestService } from '../services/file-request-service';
import { AxiosService } from '../services/axios.service';
import { FileSizePipe } from "../pipes/file-size-pipe";
import Swal from 'sweetalert2';
import { trigger, style, transition, animate } from '@angular/animations';

@Component({
  selector: 'app-guest-view',
  imports: [FileSizePipe],
  templateUrl: './guest-view.html',
  styleUrl: './guest-view.scss',
  animations: [
    trigger('fadeInOut', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-10px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [ 
        animate('200ms ease-in', style({ opacity: 0, transform: 'translateY(10px)' }))
      ])
    ])
  ]
})
export class GuestView implements OnDestroy {
  file: File | null = null;
  uploadProgress: number = 0;
  requestId: string | null = null;
  folderId: number = 1;
  guestName: string = '';

  constructor(
    private fileRequestService: FileRequestService,
    private axiosService: AxiosService
  ) {
    this.fileRequestService.connectAsGuest();
  }

  ngOnDestroy() {
    this.fileRequestService.disconnect();
  }

  onFileSelected(event: any) {
    this.file = event.target.files[0];
    this.uploadProgress = 0;
    this.requestId = null;
  }

  async sendRequest() {
    if (!this.file) return;

    const { value: name } = await Swal.fire({
      title: 'Enter your name',
      input: 'text',
      inputLabel: 'Your name will be shown to the host',
      inputPlaceholder: 'Enter your name here...',
      showCancelButton: true,
      inputValidator: (value) => {
        if (!value) return 'Please enter your name';
        return null;
      }
    });

    if (!name) return;
    this.guestName = name;

    Swal.fire({
      title: 'Sending request...',
      html: 'Waiting for host approval',
      allowOutsideClick: false,
      didOpen: () => Swal.showLoading()
    });

    try {
      this.requestId = await this.fileRequestService.sendFileRequest(this.file, this.guestName);

      const approval = await this.listenForHostResponse();
      
      if (approval.approved && approval.token) {
        await this.uploadFile(approval.token);
      } else if (approval.reason === 'timeout') {
        await Swal.fire('Timeout', 'The request expired after 5 minutes', 'error');
      } else {
        await Swal.fire('Rejected', 'The host rejected your file upload request', 'error');
      }
      
    } catch (error) {
      await Swal.fire('Error', 'Failed to process your request', 'error');
    }
  }

  private async listenForHostResponse(): Promise<{approved: boolean, token?: string, reason?: string}> {
    return new Promise((resolve) => {
      this.fileRequestService.listenForApproval((token: string) => {
        resolve({approved: true, token});
      });

      this.fileRequestService.listenForRejection(() => {
        resolve({approved: false});
      });

      setTimeout(() => {
        resolve({approved: false, reason: 'timeout'});
      }, 300000);
    });
  }

  private async uploadFile(token: string): Promise<void> {
    Swal.fire({
        title: 'Uploading file...',
        html: `
            <div class="progress mt-3">
                <div id="upload-progress" class="progress-bar" role="progressbar" style="width: 0%"></div>
            </div>
            <p class="mt-2" id="progress-text">0%</p>
        `,
        allowOutsideClick: false,
        didOpen: () => Swal.showLoading()
    });

    try {
        await this.axiosService.uploadFile(
            `/secure-upload/${this.requestId}?folderId=${this.folderId}`,
            this.file!,
            (progress) => {
                this.uploadProgress = progress;
                const progressElement = document.getElementById('upload-progress');
                const textElement = document.getElementById('progress-text');
                if (progressElement) progressElement.style.width = `${progress}%`;
                if (textElement) textElement.textContent = `${progress}%`;
                
                if (progress === 100) {
                    textElement!.textContent = 'Processing...';
                }
            },
            { 
                headers: { 
                    Authorization: token,
                    'X-Session-Id': this.fileRequestService.getSessionId(),
                    'Content-Type': 'multipart/form-data'
                }
            }
        );

        Swal.close();
        await Swal.fire({
            icon: 'success',
            title: 'Upload complete!',
            text: `File "${this.file?.name}" was successfully processed`
        });

        this.resetUploadState();

    } catch (error: any) {
        Swal.close();
        await Swal.fire({
            icon: 'error',
            title: 'Upload failed',
            text: this.getErrorMessage(error),
        });
    }
  }

  private resetUploadState(): void {
    this.file = null;
    this.uploadProgress = 0;
    this.requestId = null;
  }

  private getErrorMessage(error: any): string {
    if (error.response) {
        return error.response.data?.message || 
              `Server error: ${error.response.status}`;
    }
    return error.message || 'Unknown error occurred';
  }
}