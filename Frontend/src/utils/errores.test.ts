import { AxiosError } from 'axios';
import { describe, expect, it } from 'vitest';
import { mensajeDeError } from './errores';

function errorDeApi(datos: unknown): AxiosError {
  const error = new AxiosError('Request failed');
  error.response = { data: datos, status: 400, statusText: 'Bad Request', headers: {}, config: {} as never };
  return error;
}

describe('mensajeDeError', () => {
  it('une el mensaje general con el detalle de cada campo', () => {
    const error = errorDeApi({ message: 'No se puede facturar', errores: { cuotaBase: 'Falta definir la cuota', comedor: 'Falta el comedor' } });

    expect(mensajeDeError(error, 'Error')).toBe('No se puede facturar: Falta definir la cuota. Falta el comedor');
  });

  it('usa solo el mensaje cuando no hay detalle', () => {
    expect(mensajeDeError(errorDeApi({ message: 'No encontrado' }), 'Error')).toBe('No encontrado');
  });

  it('usa el texto por defecto si el error no es de la API', () => {
    expect(mensajeDeError(new Error('otro'), 'No se pudo')).toBe('No se pudo');
    expect(mensajeDeError(errorDeApi(undefined), 'No se pudo')).toBe('No se pudo');
  });
});
