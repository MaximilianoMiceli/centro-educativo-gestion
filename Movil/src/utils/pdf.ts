import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import { ApiError, tokenActual, urlDe } from '../api/cliente';

/** Descarga un PDF de la API con el token de la sesión y abre el menú para verlo o compartirlo. */
export async function descargarYAbrir(ruta: string, nombreArchivo: string): Promise<void> {
  const token = tokenActual();
  if (!token) {
    throw new ApiError(401, 'Tu sesión venció. Ingresá de nuevo.');
  }
  const destino = `${FileSystem.cacheDirectory}${nombreArchivo}`;
  const resultado = await FileSystem.downloadAsync(urlDe(ruta), destino, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (resultado.status !== 200) {
    throw new ApiError(resultado.status, 'No se pudo descargar el documento');
  }
  if (!(await Sharing.isAvailableAsync())) {
    throw new ApiError(0, 'Este dispositivo no puede abrir el documento');
  }
  await Sharing.shareAsync(resultado.uri, { mimeType: 'application/pdf', dialogTitle: nombreArchivo });
}
