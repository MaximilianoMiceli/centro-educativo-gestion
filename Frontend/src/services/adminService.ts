import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';

export const ROLES = ['ADMIN', 'DIRECCION', 'PROFESOR', 'PADRE', 'ALUMNO'] as const;
export type RolNombre = (typeof ROLES)[number];

export const TIPOS_DOCUMENTO = ['DNI', 'PASAPORTE', 'LC', 'LE'] as const;

export interface CrearUsuarioDatos {
  username: string;
  email: string;
  tipoDocumento: string;
  numeroDocumento: string;
  rol: RolNombre;
}

export interface UsuarioCreado {
  id: number;
  username: string;
  email: string;
  rol: RolNombre;
  passwordTemporal: string;
}

export interface PermisoDTO {
  codigo: string;
  descripcion: string;
}

export interface RolPermisos {
  nombre: RolNombre;
  permisos: string[];
}

class AdminService {
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

  async crearUsuario(datos: CrearUsuarioDatos): Promise<UsuarioCreado> {
    const response = await this.api.post<UsuarioCreado>('/usuarios', datos);
    return response.data;
  }

  async listarPermisos(): Promise<PermisoDTO[]> {
    const response = await this.api.get<PermisoDTO[]>('/permisos');
    return response.data;
  }

  async listarRoles(): Promise<RolPermisos[]> {
    const response = await this.api.get<RolPermisos[]>('/roles');
    return response.data;
  }

  async actualizarPermisos(rol: RolNombre, permisos: string[]): Promise<RolPermisos> {
    const response = await this.api.put<RolPermisos>(`/roles/${rol}/permisos`, { permisos });
    return response.data;
  }
}

export const adminService = new AdminService();
