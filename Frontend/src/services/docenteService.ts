import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface DocenteDTO {
  id: number;
  legajo: string;
  dni: string;
  nombre: string;
  apellido: string;
  especialidad: string;
  email: string;
  telefono: string;
  estado: 'ACTIVO' | 'INACTIVO' | 'LICENCIA';
  dniEditable: boolean;
  cursos: string[];
}

export interface DocenteAgenda {
  docenteId: number;
  docente: string;
  cursos: {
    cursoId: number;
    curso: string;
    nivel: string;
    materias: { materia: string; horarios: string[] }[];
  }[];
}

export interface DocenteFormulario extends DocenteEdicion {
  legajo: string;
}

export interface DocenteEdicion {
  dni: string;
  nombre: string;
  apellido: string;
  especialidad: string;
  email: string;
  telefono: string;
  estado: DocenteDTO['estado'];
}

class DocenteService {
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

  async agenda(id: number): Promise<DocenteAgenda> {
    const response = await this.api.get<DocenteAgenda>(`/docentes/${id}/agenda`);
    return response.data;
  }

  async crear(datos: DocenteFormulario): Promise<DocenteDTO> {
    const response = await this.api.post<DocenteDTO>('/docentes', datos);
    return response.data;
  }

  async listar(): Promise<DocenteDTO[]> {
    const response = await this.api.get<DocenteDTO[]>('/docentes');
    return response.data;
  }

  async buscarPorLegajo(legajo: string): Promise<DocenteDTO> {
    const response = await this.api.get<DocenteDTO>('/docentes/buscar/legajo', { params: { legajo } });
    return response.data;
  }

  async buscarPorDni(dni: string): Promise<DocenteDTO> {
    const response = await this.api.get<DocenteDTO>('/docentes/buscar/dni', { params: { dni } });
    return response.data;
  }

  async actualizar(id: number, datos: DocenteEdicion): Promise<DocenteDTO> {
    const response = await this.api.put<DocenteDTO>(`/docentes/${id}`, datos);
    return response.data;
  }
}

export const docenteService = new DocenteService();
