import axios from 'axios';

interface CuerpoError {
  message?: string;
  errores?: Record<string, string>;
}

/** Arma el mensaje de un error de la API: el texto general más el detalle de cada campo, si lo hay. */
export function mensajeDeError(error: unknown, porDefecto: string): string {
  if (axios.isAxiosError<CuerpoError>(error) && error.response?.data) {
    const { message, errores } = error.response.data;
    const detalle = Object.values(errores ?? {});
    if (message && detalle.length > 0) return `${message}: ${detalle.join('. ')}`;
    if (message) return message;
  }
  return porDefecto;
}
