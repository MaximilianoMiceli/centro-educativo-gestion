/**
 * Dirección del backend. En el emulador de Android, 10.0.2.2 es la PC donde corre el backend.
 * Para apuntar a producción se define EXPO_PUBLIC_API_URL al iniciar la app (ver README).
 */
export const API_URL: string = process.env.EXPO_PUBLIC_API_URL ?? 'http://10.0.2.2:8080/api/v1';

export const ROLES_PERMITIDOS: readonly string[] = ['PADRE', 'ALUMNO'];
