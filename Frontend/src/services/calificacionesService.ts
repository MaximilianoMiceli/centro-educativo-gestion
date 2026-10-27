import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface DictadoCalificable {
  id: number;
  materia: string;
  curso: string;
  nivel: string;
  docente: string;
}

export interface Evaluacion {
  id: number;
  titulo: string;
  tipo: TipoEvaluacion;
  periodo: number;
  fecha: string;
}

export type TipoEvaluacion = 'EXAMEN' | 'TRABAJO_PRACTICO' | 'ORAL' | 'OTRA';

export const ETIQUETA_TIPO: Record<TipoEvaluacion, string> = {
  EXAMEN: 'Examen',
  TRABAJO_PRACTICO: 'Trabajo práctico',
  ORAL: 'Oral',
  OTRA: 'Otra',
};

export interface NotaAlumno {
  alumnoId: number;
  legajo: string;
  alumno: string;
  nota: number | null;
}

export interface Planilla {
  evaluacionId: number;
  titulo: string;
  tipo: TipoEvaluacion;
  materia: string;
  curso: string;
  fecha: string;
  alumnos: NotaAlumno[];
}

export interface NuevaEvaluacion {
  titulo: string;
  tipo: TipoEvaluacion;
  periodo: number;
  fecha: string;
}

export interface EvaluacionNota {
  titulo: string;
  tipo: TipoEvaluacion;
  periodo: number;
  fecha: string;
  nota: number | null;
}

export interface MateriaCalificaciones {
  materia: string;
  docente: string;
  promedio: number | null;
  evaluaciones: EvaluacionNota[];
}

class CalificacionesService {
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

  async dictados(): Promise<DictadoCalificable[]> {
    return (await this.api.get<DictadoCalificable[]>('/calificaciones/dictados')).data;
  }

  async evaluaciones(dictadoId: number): Promise<Evaluacion[]> {
    return (await this.api.get<Evaluacion[]>(`/calificaciones/dictados/${dictadoId}/evaluaciones`)).data;
  }

  async crearEvaluacion(dictadoId: number, datos: NuevaEvaluacion): Promise<Evaluacion> {
    return (await this.api.post<Evaluacion>(`/calificaciones/dictados/${dictadoId}/evaluaciones`, datos)).data;
  }

  async planilla(evaluacionId: number): Promise<Planilla> {
    return (await this.api.get<Planilla>(`/calificaciones/evaluaciones/${evaluacionId}`)).data;
  }

  async guardarNotas(evaluacionId: number, notas: { alumnoId: number; nota: number | null }[]): Promise<Planilla> {
    return (await this.api.put<Planilla>(`/calificaciones/evaluaciones/${evaluacionId}`, { notas })).data;
  }

  /** Calificaciones de un alumno visible para el usuario (él mismo o su hijo). */
  async deAlumno(alumnoId: number): Promise<MateriaCalificaciones[]> {
    return (await this.api.get<MateriaCalificaciones[]>(`/mi-informacion/alumnos/${alumnoId}/calificaciones`)).data;
  }
}

export const calificacionesService = new CalificacionesService();
