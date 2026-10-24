import type { RolNombre, UsuarioLista } from '../services/adminService';

export function filtrarUsuarios(usuarios: UsuarioLista[], texto: string, rol: RolNombre | ''): UsuarioLista[] {
  const termino = texto.trim().toLowerCase();
  return usuarios.filter((u) =>
    (rol === '' || u.rol === rol) &&
    (termino === '' ||
      u.username.toLowerCase().includes(termino) ||
      (u.email ?? '').toLowerCase().includes(termino) ||
      (u.numeroDocumento ?? '').toLowerCase().includes(termino)));
}
