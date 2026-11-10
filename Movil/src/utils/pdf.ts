import * as FileSystem from 'expo-file-system/legacy';
import * as Sharing from 'expo-sharing';
import { Platform } from 'react-native';
import { ApiError, tokenActual, urlDe } from '../api/cliente';

/** En el navegador no hay sistema de archivos ni menú de compartir: se baja el PDF y se abre en otra pestaña. */
async function descargarEnNavegador(ruta: string, nombreArchivo: string, token: string): Promise<void> {
  const respuesta = await fetch(urlDe(ruta), { headers: { Authorization: `Bearer ${token}` } });
  if (!respuesta.ok) {
    throw new ApiError(respuesta.status, 'No se pudo descargar el documento');
  }
  const enlace = document.createElement('a');
  enlace.href = URL.createObjectURL(await respuesta.blob());
  enlace.download = nombreArchivo;
  enlace.target = '_blank';
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  setTimeout(() => URL.revokeObjectURL(enlace.href), 60_000);
}

/** Descarga un PDF de la API con el token de la sesión y abre el menú para verlo o compartirlo. */
export async function descargarYAbrir(ruta: string, nombreArchivo: string): Promise<void> {
  const token = tokenActual();
  if (!token) {
    throw new ApiError(401, 'Tu sesión venció. Ingresá de nuevo.');
  }
  if (Platform.OS === 'web') {
    return descargarEnNavegador(ruta, nombreArchivo, token);
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
