import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class FileIconService {
  private iconMap: Record<string, string> = {
    'pdf': 'bi-file-earmark-pdf-fill text-danger',
    'doc': 'bi-file-earmark-word-fill text-primary',
    'docx': 'bi-file-earmark-word-fill text-primary',
    'xls': 'bi-file-earmark-excel-fill text-success',
    'xlsx': 'bi-file-earmark-excel-fill text-success',
    'ppt': 'bi-file-earmark-ppt-fill text-warning',
    'pptx': 'bi-file-earmark-ppt-fill text-warning',
    
    'mp4': 'bi-file-earmark-play-fill text-success',
    'mov': 'bi-file-earmark-play-fill text-success',
    'avi': 'bi-file-earmark-play-fill text-success',

    'mp3': 'bi-file-earmark-music-fill text-primary',
    'wav': 'bi-file-earmark-music-fill text-primary',
    'ogg': 'bi-file-earmark-music-fill text-primary',
    'flac': 'bi-file-music-fill text-primary',
    
    'jpg': 'bi-file-earmark-image-fill text-info',
    'png': 'bi-file-earmark-image-fill text-info',
    'gif': 'bi-file-earmark-image-fill text-info',

    'zip': 'bi-file-earmark-zip-fill text-secondary',
    'rar': 'bi-file-earmark-zip-fill text-secondary',
    '7z': 'bi-file-earmark-zip-fill text-secondary',
    
    'default': 'bi-file-earmark-fill text-secondary'
  };

  getIcon(item: any): string {
    if (this.getType(item) === "Folder")
      return 'bi-folder-fill text-warning';
    
    const filename = item.name || '';
    const extension = filename.split('.').pop()?.toLowerCase() || 'default';
    return this.iconMap[extension] || this.iconMap['default'];
  }

  getType(item: any): string {
    return item.contents !== undefined ? 'Folder' : 'File';
  }
}