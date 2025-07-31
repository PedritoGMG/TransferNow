export interface FileRequest {
  requestId: string;
  fileName: string;
  fileType: string;
  mimeType: string;
  fileSize: number;
  senderName: string;
  fileHash: string;
  senderSessionId: string;
}