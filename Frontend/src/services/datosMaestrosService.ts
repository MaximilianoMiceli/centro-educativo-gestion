import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface NivelDTO {
  id: number;
  nombre: string;
}

export interface CursoDTO {
  id: number;
  nivelId: number;
  nivel: string;
  anio: number;
  seccion: string;
  descripcion: string;
}

export interface MateriaDTO {
  id: number;
  nombre: string;
}

export interface DictadoDTO {
  id: number;
  docenteId: number;
  docente: string;
  materiaId: number;
  materia: string;
  cursoId: number;
  curso: string;
}

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

export const DIAS = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

class DatosMaestrosService {
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

  async listarNiveles(): Promise<NivelDTO[]> {
    return (await this.api.get<NivelDTO[]>('/niveles')).data;
  }

  async crearNivel(nombre: string): Promise<NivelDTO> {
    return (await this.api.post<NivelDTO>('/niveles', { nombre })).data;
  }

  async listarCursos(nivelId?: number): Promise<CursoDTO[]> {
    const params = nivelId === undefined ? {} : { nivelId };
    return (await this.api.get<CursoDTO[]>('/cursos', { params })).data;
  }

  async crearCurso(datos: { nivelId: number; anio: number; seccion: string }): Promise<CursoDTO> {
    return (await this.api.post<CursoDTO>('/cursos', datos)).data;
  }

  async listarMaterias(): Promise<MateriaDTO[]> {
    return (await this.api.get<MateriaDTO[]>('/materias')).data;
  }

  async crearMateria(nombre: string): Promise<MateriaDTO> {
    return (await this.api.post<MateriaDTO>('/materias', { nombre })).data;
  }

  async listarAsignaciones(): Promise<DictadoDTO[]> {
    return (await this.api.get<DictadoDTO[]>('/dictados')).data;
  }

  async asignar(datos: { docenteId: number; materiaId: number; cursoId: number }): Promise<DictadoDTO> {
    return (await this.api.post<DictadoDTO>('/dictados', datos)).data;
  }

  async listarHorarios(dictadoId: number): Promise<HorarioDTO[]> {
    return (await this.api.get<HorarioDTO[]>('/horarios', { params: { dictadoId } })).data;
  }

  async crearHorario(datos: {
    dictadoId: number;
    diaSemana: number;
    horaInicio: string;
    horaFin: string;
  }): Promise<HorarioDTO> {
    return (await this.api.post<HorarioDTO>('/horarios', datos)).data;
  }
}

export const datosMaestrosService = new DatosMaestrosService();
