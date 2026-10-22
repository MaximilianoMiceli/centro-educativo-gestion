import { describe, expect, it } from 'vitest';
import { debeCambiarPasswordDelToken, rolDelToken } from './token';

function jwtCon(payload: Record<string, unknown>): string {
  const codificar = (valor: unknown) =>
    btoa(JSON.stringify(valor)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${codificar({ alg: 'HS512' })}.${codificar(payload)}.firma`;
}

describe('token', () => {
  it('lee el rol del payload', () => {
    expect(rolDelToken(jwtCon({ rol: 'ADMIN' }))).toBe('ADMIN');
  });

  it('indica si debe cambiar la contraseña', () => {
    expect(debeCambiarPasswordDelToken(jwtCon({ debeCambiarPassword: true }))).toBe(true);
    expect(debeCambiarPasswordDelToken(jwtCon({ debeCambiarPassword: false }))).toBe(false);
  });

  it('no falla con tokens mal formados', () => {
    expect(rolDelToken('no-es-un-jwt')).toBeNull();
    expect(debeCambiarPasswordDelToken('no-es-un-jwt')).toBe(false);
  });
});
