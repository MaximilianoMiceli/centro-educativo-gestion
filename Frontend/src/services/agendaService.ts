import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';
import type { DocenteAgenda } from './docenteService';

class AgendaService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_URL,
      timeout: 5000,
    });
  }

  setToken(token: string): void {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  /** Agenda del docente vinculado al usuario que consulta. */
  async miAgenda(): Promise<DocenteAgenda> {
    return (await this.api.get<DocenteAgenda>('/mi-agenda')).data;
  }
}

export const agendaService = new AgendaService();
