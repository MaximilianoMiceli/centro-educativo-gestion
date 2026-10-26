import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface HorarioDTO {
  id: number;
  dictadoId: number;
  materia: string;
  curso: string;
  docente: string;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

class HorariosService {
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

  async listar(filtro: { cursoId?: number; docenteId?: number }): Promise<HorarioDTO[]> {
    return (await this.api.get<HorarioDTO[]>('/horarios', { params: filtro })).data;
  }
}

export const horariosService = new HorariosService();
