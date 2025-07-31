import { Injectable } from '@angular/core';
import { AxiosService } from './axios.service';

@Injectable({
  providedIn: 'root'
})
export class GenerateLink {
  private apiUrl = `/links`;
  
  constructor(private axiosService: AxiosService) { }

  async generateTemporaryLink(endpoint: string, hours: number = 24): Promise<string> {
    try {
      const response = await this.axiosService.post<string>(
        `${this.apiUrl}/generate`, 
        null,
        {
          params: {
            endpointPath: endpoint,
            hours: hours
          }
        }
      );
      
      return `${response}`;
    } catch (error) {
      throw error;
    }
  }
}
