import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface InscriptoIdioma {
  alumnoId: number;
  legajo: string;
  alumno: string;
  nivel: string;
  curso: string;
  idioma: string;
  fechaInscripcion: string;
}

export interface InscriptoApoyo {
  alumnoId: number;
  legajo: string;
  alumno: string;
  nivel: string;
  curso: string;
  fechaInicio: string;
  observaciones: string | null;
}

export interface AtencionListado {
  id: number;
  alumnoId: number;
  legajo: string;
  alumno: string;
  nivel: string;
  curso: string;
  fechaHora: string;
  motivo: string;
  observaciones: string | null;
  registradoPor: string | null;
}

export interface FiltrosListado {
  nivelId?: number;
  cursoId?: number;
  idiomaId?: number;
}

class ListadosServiciosService {
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

  async idiomas(filtros: FiltrosListado): Promise<InscriptoIdioma[]> {
    return (await this.api.get<InscriptoIdioma[]>('/servicios/idiomas', { params: filtros })).data;
  }

  async apoyo(filtros: FiltrosListado): Promise<InscriptoApoyo[]> {
    return (await this.api.get<InscriptoApoyo[]>('/servicios/apoyo', { params: filtros })).data;
  }

  async enfermeria(filtros: FiltrosListado): Promise<AtencionListado[]> {
    return (await this.api.get<AtencionListado[]>('/servicios/enfermeria', { params: filtros })).data;
  }
}

export const listadosServiciosService = new ListadosServiciosService();
