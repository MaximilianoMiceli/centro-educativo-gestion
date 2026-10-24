import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { API_URL } from '../config';
import { nombreDesdeContentDisposition, paramsDe } from '../utils/reportes';
import type { FiltrosSeleccionados } from '../utils/reportes';

export interface CatalogoReporte {
  codigo: string;
  titulo: string;
  slug: string;
}

export interface SeccionReporte {
  titulo: string;
  columnas: string[];
  filas: string[][];
}

export interface Reporte {
  codigo: string;
  titulo: string;
  filtros: string[];
  secciones: SeccionReporte[];
}

export type FormatoDescarga = 'pdf' | 'excel';

class ReportesService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: API_URL,
      timeout: 15000,
    });
  }

  setToken(token: string): void {
    this.api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
  }

  async catalogo(): Promise<CatalogoReporte[]> {
    return (await this.api.get<CatalogoReporte[]>('/reportes')).data;
  }

  async generar(slug: string, filtros: FiltrosSeleccionados): Promise<Reporte> {
    return (await this.api.get<Reporte>(`/reportes/${slug}`, { params: paramsDe(filtros) })).data;
  }

  async descargar(slug: string, formato: FormatoDescarga, filtros: FiltrosSeleccionados): Promise<void> {
    const respuesta = await this.api.get<Blob>(`/reportes/${slug}/${formato}`, {
      params: paramsDe(filtros),
      responseType: 'blob',
    });
    const extension = formato === 'pdf' ? 'pdf' : 'xlsx';
    const nombre = nombreDesdeContentDisposition(respuesta.headers['content-disposition']) ?? `${slug}.${extension}`;

    const url = URL.createObjectURL(respuesta.data);
    const enlace = document.createElement('a');
    enlace.href = url;
    enlace.download = nombre;
    document.body.appendChild(enlace);
    enlace.click();
    enlace.remove();
    URL.revokeObjectURL(url);
  }
}

export const reportesService = new ReportesService();
