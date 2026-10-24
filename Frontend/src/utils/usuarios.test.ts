import { describe, expect, it } from 'vitest';
import { filtrarUsuarios } from './usuarios';
import type { UsuarioLista } from '../services/adminService';

const base = { activo: true, debeCambiarPassword: false };
const usuarios: UsuarioLista[] = [
  { ...base, id: 1, username: 'admin', email: 'admin@centro.local', rol: 'ADMIN', numeroDocumento: null },
  { ...base, id: 2, username: 'profesor1', email: 'p1@centro.local', rol: 'PROFESOR', numeroDocumento: '22333444' },
  { ...base, id: 3, username: 'padre1', email: 'padre@mail.com', rol: 'PADRE', numeroDocumento: '30111222' },
];

describe('filtrarUsuarios', () => {
  it('sin texto ni rol devuelve todos', () => {
    expect(filtrarUsuarios(usuarios, '', '')).toHaveLength(3);
  });

  it('busca por username, correo o documento sin distinguir mayúsculas', () => {
    expect(filtrarUsuarios(usuarios, 'PROF', '').map((u) => u.id)).toEqual([2]);
    expect(filtrarUsuarios(usuarios, 'mail.com', '').map((u) => u.id)).toEqual([3]);
    expect(filtrarUsuarios(usuarios, '22333', '').map((u) => u.id)).toEqual([2]);
  });

  it('combina la búsqueda con el filtro de rol', () => {
    expect(filtrarUsuarios(usuarios, 'p', 'PADRE').map((u) => u.id)).toEqual([3]);
    expect(filtrarUsuarios(usuarios, 'admin', 'PADRE')).toEqual([]);
  });
});
