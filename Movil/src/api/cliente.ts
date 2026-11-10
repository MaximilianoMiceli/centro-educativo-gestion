import { API_URL } from '../config';

/** Error de la API: trae el estado HTTP, el mensaje y los errores por campo si el servidor los manda. */
export class ApiError extends Error {
  status: number;
  errores: Record<string, string>;

  constructor(status: number, mensaje: string, errores: Record<string, string> = {}) {
    super(mensaje);
    this.name = 'ApiError';
    this.status = status;
    this.errores = errores;
  }

  /** El mensaje general más el detalle de cada campo, para mostrarlo en pantalla. */
  textoCompleto(): string {
    const detalle = Object.values(this.errores);
    return detalle.length ? `${this.message}: ${detalle.join('. ')}` : this.message;
  }
}

interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  cuerpo?: unknown;
  formulario?: FormData;
  parametros?: Record<string, string | undefined>;
  /** En el login un 401 significa credenciales inválidas, no sesión vencida. */
  sinSesion?: boolean;
}

let token: string | null = null;
let alExpirarSesion: (() => void) | null = null;

export function fijarToken(nuevo: string | null): void {
  token = nuevo;
}

export function tokenActual(): string | null {
  return token;
}

export function alSesionExpirada(callback: (() => void) | null): void {
  alExpirarSesion = callback;
}

export function urlDe(ruta: string, parametros?: Record<string, string | undefined>): string {
  const consulta = Object.entries(parametros ?? {})
    .filter((par): par is [string, string] => par[1] !== undefined && par[1] !== '')
    .map(([clave, valor]) => `${encodeURIComponent(clave)}=${encodeURIComponent(valor)}`)
    .join('&');
  return `${API_URL}${ruta}${consulta ? `?${consulta}` : ''}`;
}

export async function pedir<T>(ruta: string, opciones: Opciones = {}): Promise<T> {
  const headers: Record<string, string> = { Accept: 'application/json' };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  let body: string | FormData | undefined;
  if (opciones.formulario) {
    body = opciones.formulario;
  } else if (opciones.cuerpo !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(opciones.cuerpo);
  }

  let respuesta: Response;
  try {
    respuesta = await fetch(urlDe(ruta, opciones.parametros), { method: opciones.metodo ?? 'GET', headers, body });
  } catch {
    throw new ApiError(0, 'No se pudo conectar con el servidor. Revisá tu conexión e intentá de nuevo.');
  }

  if (respuesta.ok) {
    return (await respuesta.json()) as T;
  }
  if (respuesta.status === 401 && !opciones.sinSesion) {
    alExpirarSesion?.();
    throw new ApiError(401, 'Tu sesión venció. Ingresá de nuevo.');
  }
  throw await errorDe(respuesta, opciones.sinSesion === true);
}

async function errorDe(respuesta: Response, esLogin: boolean): Promise<ApiError> {
  if (respuesta.status === 401 && esLogin) {
    return new ApiError(401, 'Usuario o contraseña incorrectos');
  }
  try {
    const datos = (await respuesta.json()) as { message?: string; errores?: Record<string, string> };
    return new ApiError(respuesta.status, datos.message ?? mensajeGenerico(respuesta.status), datos.errores ?? {});
  } catch {
    return new ApiError(respuesta.status, mensajeGenerico(respuesta.status));
  }
}

function mensajeGenerico(estado: number): string {
  if (estado === 403) {
    return 'No tenés permiso para hacer esto';
  }
  if (estado === 404) {
    return 'No se encontró lo que buscabas';
  }
  return 'El servidor no pudo completar la operación';
}
