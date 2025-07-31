import { Pipe, PipeTransform } from '@angular/core';
import { isFolder } from '../model/Folder';
import { FileSystemItem } from '../model/FileSystemItem';

@Pipe({
  name: 'orderByType'
})
export class OrderByTypePipe implements PipeTransform {
  transform(items: FileSystemItem[]): FileSystemItem[] {
    if (!items) return [];

    return items.slice().sort((a, b) => {
        if (isFolder(a) && !isFolder(b)) return -1;
        if (!isFolder(a) && isFolder(b)) return 1;
        return 0;
    });
  }
}