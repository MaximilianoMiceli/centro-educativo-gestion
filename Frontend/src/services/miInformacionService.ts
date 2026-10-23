import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';
import type { AlumnoFicha, AlumnoMateria } from './alumnosAdminService';
import type { ServiciosAlumno } from './serviciosService';

export interface AlumnoResumen {
  id: number;
  legajo: string;
  alumno: string;
  curso: string;
  nivel: string;
}

export interface MiInformacion {
  ficha: AlumnoFicha;
  materias: AlumnoMateria[];
  servicios: ServiciosAlumno;
}

class MiInformacionService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({ baseURL: API_URL, timeout: 5000 });
  }

  setToken(token: string): void {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  async alumnos(): Promise<AlumnoResumen[]> {
    return (await this.api.get<AlumnoResumen[]>('/mi-informacion/alumnos')).data;
  }

  async detalle(id: number): Promise<MiInformacion> {
    return (await this.api.get<MiInformacion>(`/mi-informacion/alumnos/${id}`)).data;
  }
}

export const miInformacionService = new MiInformacionService();
