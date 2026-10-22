import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface AlumnoFicha {
  id: number;
  legajo: string;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  domicilio: string | null;
  telefono: string | null;
  email: string | null;
  cursoId: number;
  cursoDescripcion: string;
  nivelNombre: string;
  estado: 'ACTIVO' | 'INACTIVO' | 'EGRESADO';
}

export interface DatosAlumno {
  legajo: string;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  domicilio: string;
  telefono: string;
  email: string;
  cursoId: number;
  estado: AlumnoFicha['estado'];
}

export interface AlumnoMateria {
  materia: string;
  docente: string;
  horarios: string[];
}

class AlumnosAdminService {
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

  async listar(filtros: { busqueda?: string; nivelId?: number; cursoId?: number } = {}): Promise<AlumnoFicha[]> {
    const params: Record<string, string | number> = {};
    if (filtros.busqueda) params.busqueda = filtros.busqueda;
    if (filtros.nivelId !== undefined) params.nivelId = filtros.nivelId;
    if (filtros.cursoId !== undefined) params.cursoId = filtros.cursoId;
    return (await this.api.get<AlumnoFicha[]>('/alumnos', { params })).data;
  }

  async crear(datos: DatosAlumno): Promise<AlumnoFicha> {
    return (await this.api.post<AlumnoFicha>('/alumnos', datos)).data;
  }

  async modificar(id: number, datos: DatosAlumno): Promise<AlumnoFicha> {
    return (await this.api.put<AlumnoFicha>(`/alumnos/${id}`, datos)).data;
  }

  async materias(id: number): Promise<AlumnoMateria[]> {
    return (await this.api.get<AlumnoMateria[]>(`/alumnos/${id}/materias`)).data;
  }
}

export const alumnosAdminService = new AlumnosAdminService();
