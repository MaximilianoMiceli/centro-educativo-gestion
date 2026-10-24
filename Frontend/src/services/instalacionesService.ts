import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface InstalacionDTO {
  id: number;
  nombre: string;
  tipo: TipoInstalacion;
  ubicacion: string | null;
  capacidad: number | null;
  estado: EstadoInstalacion;
  descripcion: string | null;
}

export type TipoInstalacion = 'PILETA' | 'CANCHA' | 'PISTA' | 'GIMNASIO' | 'COMEDOR' | 'LABORATORIO' | 'ENFERMERIA' | 'OTRA';
export type EstadoInstalacion = 'ACTIVA' | 'EN_MANTENIMIENTO' | 'INACTIVA';

export interface SolicitudInstalacion {
  nombre: string;
  tipo: TipoInstalacion;
  ubicacion: string | null;
  capacidad: number | null;
  estado: EstadoInstalacion;
  descripcion: string | null;
}

export const ETIQUETA_TIPO: Record<TipoInstalacion, string> = {
  PILETA: 'Pileta',
  CANCHA: 'Cancha',
  PISTA: 'Pista',
  GIMNASIO: 'Gimnasio',
  COMEDOR: 'Comedor',
  LABORATORIO: 'Laboratorio',
  ENFERMERIA: 'Enfermería',
  OTRA: 'Otra',
};

export const ETIQUETA_ESTADO: Record<EstadoInstalacion, string> = {
  ACTIVA: 'Activa',
  EN_MANTENIMIENTO: 'En mantenimiento',
  INACTIVA: 'Inactiva',
};

class InstalacionesService {
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

  async listar(): Promise<InstalacionDTO[]> {
    return (await this.api.get<InstalacionDTO[]>('/instalaciones')).data;
  }

  async crear(datos: SolicitudInstalacion): Promise<InstalacionDTO> {
    return (await this.api.post<InstalacionDTO>('/instalaciones', datos)).data;
  }

  async modificar(id: number, datos: SolicitudInstalacion): Promise<InstalacionDTO> {
    return (await this.api.put<InstalacionDTO>(`/instalaciones/${id}`, datos)).data;
  }
}

export const instalacionesService = new InstalacionesService();
