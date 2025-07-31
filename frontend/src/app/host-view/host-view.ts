import { AfterViewInit, Component, OnInit } from '@angular/core';
import { FileSystemItemService } from '../services/file-system-item.service';
import { FileSystemItem } from '../model/FileSystemItem';
import { FileSizePipe } from "../pipes/file-size-pipe";
import { FileIconService } from '../services/file-icon.service';
import { isFolder } from '../model/Folder';
import { Observable } from 'rxjs/internal/Observable';
import { from } from 'rxjs/internal/observable/from';
import Swal from 'sweetalert2';
import { OrderByTypePipe } from "../pipes/order-by-type-pipe";
import { DatePipe } from '@angular/common';
import { SearchPipe } from '../pipes/search-pipe';
import { FormsModule } from '@angular/forms';
import Tooltip from 'bootstrap/js/dist/tooltip';
import { SortPipe } from '../pipes/sort-pipe';
import { GenerateLink } from '../services/generate-link';
import { AxiosService } from '../services/axios.service';
import { trigger, style, transition, animate } from '@angular/animations';


@Component({
  selector: 'app-host-view',
  imports: [FormsModule, FileSizePipe, OrderByTypePipe, DatePipe, SearchPipe, SortPipe],
  templateUrl: './host-view.html',
  styleUrl: './host-view.scss',
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
export class HostView implements OnInit, AfterViewInit {
  actualFolderIndex: number = 1;
  actualDir: Map<number, string> = new Map<number, string>();
  folderContents: Array<FileSystemItem> = [];
  searchText: string = '';
  sortColumn: string = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(public fileSystemItemService: FileSystemItemService, public iconService: FileIconService, public generateLink: GenerateLink, public AxiosService: AxiosService) {
    this.loadFolderContents();
    this.actualDir.set(1, "main");
  }
  ngOnInit(): void {
    //throw new Error('Method not implemented.');
  }

  ngAfterViewInit() {
    const tooltipTriggerList = Array.from(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach((tooltipTriggerEl) => {
      new Tooltip(tooltipTriggerEl);
    });
  }

  sortBy(column: string) {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
  }

  getCurrentDirName(): string | undefined {
    return Array.from(this.actualDir.values()).at(-1);
  }

  goTo(id:number) {
    this.actualFolderIndex = id;
    this.loadFolderContents();
  }

  async goToPublicEndpoint(endpoint: string): Promise<void> {
    const result = await this.AxiosService.getHostUrl() + endpoint;
    window.open(result, '_blank');
  }

  getSize(id: number): Observable<any> {
    return from(this.fileSystemItemService.getSize(id));
  }

  async selectFileSource(): Promise<void> {
    try {
      const { value: sourceChoice } = await Swal.fire({
        title: 'Add file',
        text: 'Choose how you want to provide the file:',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: 'Enter path manually',
        cancelButtonText: 'Cancel',
        showDenyButton: true,
        denyButtonText: 'Upload file'
      });

      if (sourceChoice === undefined) return;

      if (sourceChoice === true) {
        const { value: filePath, isConfirmed } = await Swal.fire({
          title: 'Enter file path',
          input: 'text',
          inputLabel: 'Full path of the file',
          inputPlaceholder: 'e.g. C:/Users/usuario/Desktop/file.txt',
          inputValidator: (value) => {
            if (!value) return 'Path cannot be empty';
            return null;
          },
          showCancelButton: true
        });

        if (isConfirmed && filePath) {
          await this.fileSystemItemService.addFileFromPath(filePath.trim(), this.actualFolderIndex);
        }

      } else {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = '*/*';

        input.onchange = async () => {
          if (!input.files || input.files.length === 0) return;

          const file = input.files[0];

          Swal.fire({
            title: 'Uploading file...',
            html: `Uploading <b>${file.name}</b>`,
            allowOutsideClick: false,
            didOpen: () => {
              Swal.showLoading();
            }
          });

          try {
            await this.fileSystemItemService.uploadFile(file, this.actualFolderIndex);
            await Swal.fire('Success', `File <b>${file.name}</b> uploaded successfully`, 'success');
          } catch (error) {
            await Swal.fire('Error', `Failed to upload <b>${file.name}</b>`, 'error');
          } finally {
            await this.loadFolderContents();
          }
        };

        input.click();
      }

    } catch (error) {
      await Swal.fire('Error', 'Something went wrong', 'error');
    } finally {
      await this.loadFolderContents();
    }
  }

  async download(id: number, filename?: string): Promise<void> {
    Swal.fire({
      title: 'Preparing download...',
      html: 'Please wait while the file is prepared.',
      allowOutsideClick: false,
      allowEscapeKey: false,
      didOpen: () => {
        Swal.showLoading();
      }
    });

    const result = await this.fileSystemItemService.downloadAndSaveFile(id, filename);

    if (result) {
      Swal.fire('Success', 'File received!', 'success');
    } else {
      Swal.fire('Error', 'Could not download file', 'error');
    }
  }



  async createFolder(): Promise<void> {
    try {
      const { value: folderName, isConfirmed } = await Swal.fire({
        title: 'Create new folder',
        input: 'text',
        inputLabel: 'Folder name',
        inputPlaceholder: 'Enter folder name',
        showCancelButton: true,
        confirmButtonText: 'Create',
        cancelButtonText: 'Cancel',
        inputValidator: (value) => {
          if (!value) return 'Folder name cannot be empty';
          return null;
        }
      });

      if (!isConfirmed || !folderName) return;

      await this.fileSystemItemService.createFolder(folderName.trim(), this.actualFolderIndex);

      await Swal.fire('Success', `Folder <b>${folderName}</b> created!`, 'success');
    } catch (error) {
      await Swal.fire('Error', 'Could not create folder', 'error');
    } finally {
      await this.loadFolderContents();
    }
  }


  async deleteItem(item: FileSystemItem) {
    const success = await this.fileSystemItemService.confirmDelete(item);
    if (success) {
      await this.loadFolderContents();
    }
  }

  async share(endpoint: string): Promise<void> {
    try {
      const { value: hours } = await Swal.fire({
        title: 'Temporary link duration',
        text: 'How many hours should the link be valid?',
        input: 'number',
        inputAttributes: {
          min: '1',
          step: '1'
        },
        inputValue: 24,
        showCancelButton: true,
        confirmButtonText: 'Generate link',
      });

      if (!hours) {
        return;
      }

      const link = await this.AxiosService.getHostUrl() + await this.generateLink.generateTemporaryLink(endpoint, Number(hours));

      await Swal.fire({
        title: 'Temporary link generated!',
        html: `
          <p>You can share this link:</p>
          <a href="${link}" target="_blank">${link}</a>
          <br><br>
          <button id="copy-btn" class="swal2-confirm swal2-styled" style="background:#3085d6;">
            📋 Copy link
          </button>
        `,
        icon: 'success',
        showConfirmButton: true,
        confirmButtonText: 'Close',
        didOpen: () => {
          const copyBtn = document.getElementById('copy-btn');
          copyBtn?.addEventListener('click', async () => {
            await navigator.clipboard.writeText(link);
            copyBtn.innerText = "✅ Copied!";
            setTimeout(() => (copyBtn.innerText = "📋 Copy link"), 2000);
          });
        }
      });

    } catch (error) {
      await Swal.fire({
        title: 'Error',
        text: 'Failed to generate temporary link',
        icon: 'error',
        confirmButtonText: 'Ok'
      });
    }
  }

  async loadFolderContents() {
    try {
      this.folderContents = await this.fileSystemItemService.getFolderContents(this.actualFolderIndex);
      this.actualDir = await this.fileSystemItemService.getItemPathMap(this.actualFolderIndex);
    } catch (error) {
    }
  }

  isFolder(item: FileSystemItem): boolean {
    return isFolder(item);
  }
}
