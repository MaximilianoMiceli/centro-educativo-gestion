import { estaVencido, leerToken } from '../utils/jwt';

function armarToken(carga: object): string {
  const codificar = (objeto: object) => Buffer.from(JSON.stringify(objeto)).toString('base64url');
  return `${codificar({ alg: 'HS512' })}.${codificar(carga)}.firma`;
}

describe('leerToken', () => {
  it('lee los datos del usuario', () => {
    const datos = leerToken(armarToken({ sub: '7', username: 'padre1', rol: 'PADRE', debeCambiarPassword: false }));

    expect(datos).toEqual({
      usuarioId: '7',
      username: 'padre1',
      rol: 'PADRE',
      debeCambiarPassword: false,
      vence: undefined,
    });
  });

  it('respeta los acentos', () => {
    expect(leerToken(armarToken({ sub: '1', username: 'María', rol: 'PADRE' })).username).toBe('María');
  });

  it('detecta que hay que cambiar la contraseña', () => {
    expect(leerToken(armarToken({ sub: '1', rol: 'ALUMNO', debeCambiarPassword: true })).debeCambiarPassword).toBe(true);
  });

  it('rechaza un token mal formado', () => {
    expect(() => leerToken('no-es-un-token')).toThrow('Token inválido');
    expect(() => leerToken('a.b$.c')).toThrow();
  });
});

describe('estaVencido', () => {
  it('compara el vencimiento con la hora actual', () => {
    const datos = leerToken(armarToken({ sub: '1', rol: 'PADRE', exp: 1000 }));

    expect(estaVencido(datos, 999_000)).toBe(false);
    expect(estaVencido(datos, 1_000_000)).toBe(true);
  });

  it('no vence si el token no trae vencimiento', () => {
    expect(estaVencido(leerToken(armarToken({ sub: '1', rol: 'PADRE' })))).toBe(false);
  });
});
