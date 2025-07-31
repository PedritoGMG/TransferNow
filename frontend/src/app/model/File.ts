import { FileSystemItem } from "./FileSystemItem";

export interface File extends FileSystemItem {
  discriminator: 'FILE';
  hardLinkPath: string;
}

export function isFile(item: FileSystemItem): item is File {
  return 'hardLinkPath' in item;
}