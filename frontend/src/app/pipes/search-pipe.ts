import { Pipe, PipeTransform } from '@angular/core';
import { FileSystemItem } from '../model/FileSystemItem';

@Pipe({
  name: 'search'
})
export class SearchPipe implements PipeTransform {

  transform(items: FileSystemItem[], searchText: string): FileSystemItem[] {
    if (!items) return [];
    if (!searchText) return items;

    searchText = searchText.toLowerCase();
    
    return items.filter(item => {
      return (
        item.name.toLowerCase().includes(searchText) ||
        (item.size && item.size.toString().includes(searchText)) ||
        (item.createdAt && new Date(item.createdAt).toLocaleString().toLowerCase().includes(searchText))
      );
    });
  }
}
