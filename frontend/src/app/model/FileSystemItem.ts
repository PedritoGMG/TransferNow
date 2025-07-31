import { FileSystemItemBase } from "./FileSystemItemBase";
import { Folder } from "./Folder";

export interface FileSystemItem extends FileSystemItemBase {
  size: number;
  path: string;
  parent?: Folder;
}