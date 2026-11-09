import * as SecureStore from 'expo-secure-store';
import type { Sesion } from '../api/tipos';

const CLAVE = 'sesion-educar';
let enMemoria: Sesion | null = null;

/** Guarda la sesión en el almacenamiento seguro del teléfono; si no está disponible (web) queda en memoria. */
export async function guardarSesion(sesion: Sesion): Promise<void> {
  enMemoria = sesion;
  try {
    await SecureStore.setItemAsync(CLAVE, JSON.stringify(sesion));
  } catch {
    // Sin almacenamiento seguro la sesión dura hasta cerrar la app.
  }
}

export async function leerSesionGuardada(): Promise<Sesion | null> {
  try {
    const texto = await SecureStore.getItemAsync(CLAVE);
    return texto ? (JSON.parse(texto) as Sesion) : enMemoria;
  } catch {
    return enMemoria;
  }
}

export async function borrarSesion(): Promise<void> {
  enMemoria = null;
  try {
    await SecureStore.deleteItemAsync(CLAVE);
  } catch {
    // No había nada guardado.
  }
}
