import { FileSystemItem } from "./FileSystemItem";

export interface Folder extends FileSystemItem {
  discriminator: 'FOLDER';
  contents: FileSystemItem[];
}

export function isFolder(item: FileSystemItem | null | undefined): item is Folder {
  return item != null && typeof item === 'object' && 'contents' in item;
}