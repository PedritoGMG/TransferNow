import { Injectable } from '@angular/core';
import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { environment } from '../../environments/environment';
import { saveAs } from 'file-saver';

@Injectable({
  providedIn: 'root'
})
export class AxiosService {
  private axiosInstance: AxiosInstance;

  constructor() {
    this.axiosInstance = axios.create({
      baseURL: environment.apiUrl,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    this.axiosInstance.interceptors.response.use(
      (response) => response,
      (error) => {
        return Promise.reject(error);
      }
    );
  }

  public async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.get<T>(url, config);
    return response.data;
  }

  public async post<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.post<T>(url, data, config);
    return response.data;
  }

  public async put<T>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.put<T>(url, data, config);
    return response.data;
  }

  public async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.axiosInstance.delete<T>(url, config);
    return response.data;
  }

  public async getHostUrl(): Promise<string> {
    return this.get(`/host-url`);
  }

  // Métodos especiales para archivos
  public async downloadFile(url: string, filename: string): Promise<void> {
    const response = await this.axiosInstance.get(url, {
      responseType: 'blob'
    });
    saveAs(new Blob([response.data]), filename);
  }

  public async uploadFile<T = any>(
      endpoint: string,
      file: File,
      onProgress?: (progress: number) => void,
      config?: AxiosRequestConfig
  ): Promise<AxiosResponse<T>> {
      const formData = new FormData();
      formData.append('file', file);

      const fullConfig: AxiosRequestConfig = {
          ...config,
          headers: {
              ...config?.headers,
              'Content-Type': 'multipart/form-data'
          },
          onUploadProgress: (progressEvent) => {
              if (onProgress && progressEvent.total) {
                  const progress = Math.min(99, Math.round((progressEvent.loaded * 100) / progressEvent.total));
                  onProgress(progress);
              }
          }
      };

      try {
          const response = await this.axiosInstance.post<T>(endpoint, formData, fullConfig);
          if (onProgress) onProgress(100);
          return response;
      } catch (error) {
          if (axios.isAxiosError(error)) {
              throw error;
          }
          throw new Error('Network error occurred');
      }
  }
}