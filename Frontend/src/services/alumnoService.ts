import axios from 'axios';
import type { AxiosInstance } from 'axios';

export interface AlumnoDTO {
  id: number;
  legajo: string;
  dni: string;
  nombre: string;
  apellido: string;
  fechaNacimiento: string;
  domicilio: string;
  telefono: string;
  email: string;
  cursoId: number;
  cursoDescripcion: string;
  nivelNombre: string;
  estado: string;
}

export interface ErrorResponse {
  status: number;
  error: string;
  message: string;
}

class AlumnoService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: 'http://localhost:8080/api/v1',
      timeout: 5000,
    });
  }

  setToken(token: string): void {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  async obtenerPorId(id: number): Promise<AlumnoDTO> {
    const response = await this.api.get<AlumnoDTO>(`/alumnos/${id}`);
    return response.data;
  }

  async buscarPorLegajo(legajo: string): Promise<AlumnoDTO> {
    const response = await this.api.get<AlumnoDTO>('/alumnos/buscar/legajo', {
      params: { legajo },
    });
    return response.data;
  }

  async buscarPorDni(dni: string): Promise<AlumnoDTO> {
    const response = await this.api.get<AlumnoDTO>('/alumnos/buscar/dni', {
      params: { dni },
    });
    return response.data;
  }

  async buscarPorNombre(texto: string): Promise<AlumnoDTO> {
    const response = await this.api.get<AlumnoDTO>('/alumnos/buscar/nombre', {
      params: { texto },
    });
    return response.data;
  }
}

export const alumnoService = new AlumnoService();
