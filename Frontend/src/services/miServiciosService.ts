import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';
import type { ServiciosAlumno } from './serviciosService';

/** Servicios del propio alumno: el backend usa la cuenta de la sesión, no recibe un id de alumno. */
class MiServiciosService {
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

  async servicios(): Promise<ServiciosAlumno> {
    return (await this.api.get<ServiciosAlumno>('/mi-informacion/servicios')).data;
  }

  async inscribirDeporte(grupoId: number): Promise<ServiciosAlumno> {
    return (await this.api.post<ServiciosAlumno>('/mi-informacion/servicios/deportes', { grupoId })).data;
  }

  async quitarDeporte(inscripcionId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/mi-informacion/servicios/deportes/${inscripcionId}`)).data;
  }

  async asignarTransporte(recorridoId: number): Promise<ServiciosAlumno> {
    return (await this.api.put<ServiciosAlumno>('/mi-informacion/servicios/transporte', { recorridoId })).data;
  }

  async quitarTransporte(): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>('/mi-informacion/servicios/transporte')).data;
  }

  async asignarComedor(inscripto: boolean): Promise<ServiciosAlumno> {
    return (await this.api.put<ServiciosAlumno>('/mi-informacion/servicios/comedor', { inscripto })).data;
  }

  async inscribirIdioma(idiomaId: number): Promise<ServiciosAlumno> {
    return (await this.api.post<ServiciosAlumno>('/mi-informacion/servicios/idiomas', { idiomaId })).data;
  }

  async quitarIdioma(inscripcionId: number): Promise<ServiciosAlumno> {
    return (await this.api.delete<ServiciosAlumno>(`/mi-informacion/servicios/idiomas/${inscripcionId}`)).data;
  }
}

export const miServiciosService = new MiServiciosService();
