import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export interface GrupoDeporte {
  id: number;
  nivel: string;
  horario: string;
}

export interface Deporte {
  id: number;
  nombre: string;
  docenteResponsableId: number | null;
  docenteResponsable: string | null;
  grupos: GrupoDeporte[];
}

export interface Recorrido {
  id: number;
  nombre: string;
}

export interface InscripcionDeporte {
  id: number;
  deporteId: number;
  deporte: string;
  horario: string;
}

export interface Idioma {
  id: number;
  nombre: string;
}

export interface IdiomaInscripto {
  id: number;
  idiomaId: number;
  idioma: string;
  fechaInscripcion: string;
}

export interface ApoyoEstudiantil {
  fechaInicio: string;
  observaciones: string | null;
}

export interface AtencionEnfermeria {
  id: number;
  fechaHora: string;
  motivo: string;
  observaciones: string | null;
  registradoPor: string | null;
}

export interface ServiciosAlumno {
  alumnoId: number;
  alumno: string;
  nivel: string;
  deportes: InscripcionDeporte[];
  transporte: Recorrido | null;
  comedor: boolean;
  idiomas: IdiomaInscripto[];
  apoyo: ApoyoEstudiantil | null;
}

export interface DatosGrupo {
  nivelId: number;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

class ServiciosService {
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

  async listarDeportes(): Promise<Deporte[]> {
    return (await this.api.get<Deporte[]>('/deportes')).data;
  }

  async crearDeporte(nombre: string, docenteResponsableId: number | null): Promise<Deporte> {
    return (await this.api.post<Deporte>('/deportes', { nombre, docenteResponsableId })).data;
  }

  async asignarResponsable(deporteId: number, docenteId: number | null): Promise<Deporte> {
    return (await this.api.put<Deporte>(`/deportes/${deporteId}/responsable`, { docenteId })).data;
  }

  async crearGrupo(deporteId: number, datos: DatosGrupo): Promise<Deporte> {
    return (await this.api.post<Deporte>(`/deportes/${deporteId}/grupos`, datos)).data;
  }

  async listarRecorridos(): Promise<Recorrido[]> {
    return (await this.api.get<Recorrido[]>('/recorridos')).data;
  }

  async servicios(alumnoId: number): Promise<ServiciosAlumno> {
    return (await this.api.get<ServiciosAlumno>(`/alumnos/${alumnoId}/servicios`)).data;
  }

  async inscribirDeporte(alumnoId: number, grupoId: number): Promise<ServiciosAlumno> {
    return (await this.api.post<ServiciosAlumno>(`/alumnos/${alumnoId}/deportes`, { grupoId })).data;
  }

  async quitarDeporte(alumnoId: number, inscripcionId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/alumnos/${alumnoId}/deportes/${inscripcionId}`)).data;
  }

  async asignarTransporte(alumnoId: number, recorridoId: number): Promise<ServiciosAlumno> {
    return (await this.api.put<ServiciosAlumno>(`/alumnos/${alumnoId}/transporte`, { recorridoId })).data;
  }

  async quitarTransporte(alumnoId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/alumnos/${alumnoId}/transporte`)).data;
  }

  async asignarComedor(alumnoId: number, inscripto: boolean): Promise<ServiciosAlumno> {
    return (await this.api.put<ServiciosAlumno>(`/alumnos/${alumnoId}/comedor`, { inscripto })).data;
  }

  async listarIdiomas(): Promise<Idioma[]> {
    return (await this.api.get<Idioma[]>('/idiomas')).data;
  }

  async inscribirIdioma(alumnoId: number, idiomaId: number): Promise<ServiciosAlumno> {
    return (await this.api.post<ServiciosAlumno>(`/alumnos/${alumnoId}/idiomas`, { idiomaId })).data;
  }

  async quitarIdioma(alumnoId: number, inscripcionId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/alumnos/${alumnoId}/idiomas/${inscripcionId}`)).data;
  }

  async asignarApoyo(alumnoId: number, observaciones: string): Promise<ServiciosAlumno> {
    return (await this.api.put<ServiciosAlumno>(`/alumnos/${alumnoId}/apoyo`, { observaciones })).data;
  }

  async quitarApoyo(alumnoId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/alumnos/${alumnoId}/apoyo`)).data;
  }

  async atencionesEnfermeria(alumnoId: number): Promise<AtencionEnfermeria[]> {
    return (await this.api.get<AtencionEnfermeria[]>(`/alumnos/${alumnoId}/enfermeria`)).data;
  }

  async registrarAtencion(alumnoId: number, motivo: string, observaciones: string): Promise<AtencionEnfermeria> {
    return (await this.api.post<AtencionEnfermeria>(`/alumnos/${alumnoId}/enfermeria`, { motivo, observaciones })).data;
  }

  async renombrarRecorrido(recorridoId: number, nombre: string): Promise<Recorrido> {
    return (await this.api.put<Recorrido>(`/recorridos/${recorridoId}`, { nombre })).data;
  }
}

export const serviciosService = new ServiciosService();
