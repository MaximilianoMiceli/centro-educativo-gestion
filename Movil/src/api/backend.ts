import { pedir } from './cliente';
import type {
  ArchivoAdjunto,
  Deuda,
  Factura,
  Hijo,
  MiInformacion,
  PagoResumen,
  Sesion,
} from './tipos';

export interface Rango {
  desde?: string;
  hasta?: string;
}

export function iniciarSesion(username: string, password: string): Promise<Sesion & { token: string }> {
  return pedir('/auth/login', { metodo: 'POST', cuerpo: { username, password }, sinSesion: true });
}

export function cambiarPassword(passwordActual: string, passwordNueva: string): Promise<Sesion> {
  return pedir('/auth/cambiar-password', { metodo: 'POST', cuerpo: { passwordActual, passwordNueva } });
}

export function hijos(): Promise<Hijo[]> {
  return pedir('/mi-informacion/alumnos');
}

export function miInformacion(alumnoId: number): Promise<MiInformacion> {
  return pedir(`/mi-informacion/alumnos/${alumnoId}`);
}

export function facturas(alumnoId: number, rango: Rango = {}): Promise<Factura[]> {
  return pedir(`/mi-informacion/alumnos/${alumnoId}/facturas`, { parametros: { ...rango } });
}

export function deuda(alumnoId: number, rango: Rango = {}): Promise<Deuda> {
  return pedir(`/mi-informacion/alumnos/${alumnoId}/deuda`, { parametros: { ...rango } });
}

export function factura(facturaId: number): Promise<Factura> {
  return pedir(`/mi-informacion/facturas/${facturaId}`);
}

/** Arma el formulario del pago: los ítems elegidos, la fecha de la transferencia y el archivo adjunto. */
export function armarFormularioPago(itemIds: number[], fechaPago: string, archivo: ArchivoAdjunto): FormData {
  const formulario = new FormData();
  itemIds.forEach((id) => formulario.append('itemIds', String(id)));
  formulario.append('fechaPago', fechaPago);
  if ('blob' in archivo) {
    formulario.append('archivo', archivo.blob, archivo.name);
  } else {
    formulario.append('archivo', { uri: archivo.uri, name: archivo.name, type: archivo.type } as unknown as Blob);
  }
  return formulario;
}

export function registrarPago(
  facturaId: number,
  itemIds: number[],
  fechaPago: string,
  archivo: ArchivoAdjunto,
): Promise<PagoResumen> {
  return pedir(`/mi-informacion/facturas/${facturaId}/pagos`, {
    metodo: 'POST',
    formulario: armarFormularioPago(itemIds, fechaPago, archivo),
  });
}
