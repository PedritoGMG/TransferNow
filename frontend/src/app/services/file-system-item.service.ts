import { Injectable } from '@angular/core';
import saveAs from 'file-saver';
import Swal from 'sweetalert2';
import { FileSystemItem } from '../model/FileSystemItem';
import { isFolder } from '../model/Folder';
import { isFile } from '../model/File';
import { AxiosService } from './axios.service';

@Injectable({
  providedIn: 'root'
})
export class FileSystemItemService {
  private apiUrl = `/filesystem`;

  constructor(private axiosService: AxiosService) { }

  async getItemPath(id: number): Promise<string> {
    return this.axiosService.get<string>(`${this.apiUrl}/${id}/path`);
  }

  async getSize(id: number): Promise<number> {
    return this.axiosService.get<number>(`${this.apiUrl}/getSize/${id}`);
  }

  async getItemPathMap(id: number): Promise<Map<number, string>> {
    const obj = await this.axiosService.get<Record<string, string>>(`${this.apiUrl}/pathMap/${id}`);
    return new Map<number, string>(
      Object.entries(obj).map(([key, value]) => [parseInt(key), value])
    );
  }

  async uploadFile(file: File, folderId: number): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);
    
    return this.axiosService.post(`${this.apiUrl}/addFile`, formData, {
      params: { idFolder: folderId },
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  }

  async addFileFromPath(path: string, folderId: number): Promise<any> {
    return this.axiosService.post(`${this.apiUrl}/addFileFromPath`, path, {
      params: { idFolder: folderId },
      headers: {
        'Content-Type': 'text/plain'
      }
    });
  }

  async createFolder(name: string, folderId: number): Promise<any> {
    return this.axiosService.post(`${this.apiUrl}/createFolder`, null, {
      params: { name, idFolder: folderId }
    });
  }

  async getFolderContents(folderId: number): Promise<any> {
    return this.axiosService.get(`${this.apiUrl}/getContentsFromFolder`, {
      params: { idFolder: folderId }
    });
  }

  async deleteFile(id: number, fullDelete: boolean): Promise<void> {
    return this.axiosService.delete<void>(`${this.apiUrl}/deleteFileByID`, {
      params: { id, fullDelete }
    });
  }

  async deleteFolder(id: number, fullDelete: boolean): Promise<void> {
    return this.axiosService.delete<void>(`${this.apiUrl}/deleteFolderByID`, {
      params: { id, fullDelete }
    });
  }

  downloadItem(id: number): Promise<Blob> {
    return this.axiosService.get<Blob>(`${this.apiUrl}/download/${id}`, {
      responseType: 'blob',
      timeout: 300000 // 5 minutes timeout
    }).then(response => response);
  }
  
  async downloadAndSaveFile(id: number, filename?: string): Promise<boolean> {
    try {
      const blob = await this.downloadItem(id);

      if (!blob || blob.size === 0) {
        return false;
      }

      const finalFilename = filename || `download-${id}.zip`;
      saveAs(blob, finalFilename);

      return true;
    } catch (error) {
      return false;
    }
  }

  async confirmDelete(item: FileSystemItem): Promise<boolean> {
    try {
      const deleteConfirm = await Swal.fire({
        title: isFile(item) ? "Delete file?" : "Delete folder?",
        html: isFolder(item)
          ? `This will permanently delete <b>${item.name}</b> and all its contents!`
          : `Are you sure you want to delete <b>${item.name}</b>?`,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#d33',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel'
      });

      if (!deleteConfirm.isConfirmed) return false;

      const fullDeleteConfirm = await Swal.fire({
        title: 'Delete permanently?',
        html: isFolder(item)
          ? `Delete all folder contents from storage? <b>This cannot be undone!</b>`
          : `Delete file from storage (not just remove reference)?`,
        icon: 'question',
        showDenyButton: true,
        confirmButtonText: 'Full delete',
        denyButtonText: 'Remove reference only',
        showCancelButton: true,
        cancelButtonText: 'Cancel entire operation'
      });

      if (fullDeleteConfirm.isDismissed) return false;

      const fullDelete = fullDeleteConfirm.isConfirmed;
      
      if (isFolder(item)) {
        await this.deleteFolder(item.id, fullDelete);
      } else {
        await this.deleteFile(item.id, fullDelete);
      }

      await Swal.fire(
        'Deleted!',
        `${isFolder(item) ? 'Folder' : 'File'} ${fullDelete ? 'permanently deleted' : 'reference removed'}`,
        'success'
      );
      return true;
    } catch (error) {
      await Swal.fire('Error', 'Could not delete item', 'error');
      return false;
    }
  }
}