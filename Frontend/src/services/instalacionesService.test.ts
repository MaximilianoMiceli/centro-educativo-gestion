import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get, post, put } = vi.hoisted(() => ({ get: vi.fn(), post: vi.fn(), put: vi.fn() }));

vi.mock('axios', () => ({
  default: {
    create: () => ({ defaults: { headers: { common: {} } }, get, post, put }),
  },
}));

import { instalacionesService } from './instalacionesService';

describe('instalacionesService', () => {
  beforeEach(() => {
    get.mockReset();
    post.mockReset();
    put.mockReset();
    get.mockResolvedValue({ data: [] });
    post.mockResolvedValue({ data: {} });
    put.mockResolvedValue({ data: {} });
  });

  it('lista las instalaciones', async () => {
    await instalacionesService.listar();

    expect(get).toHaveBeenCalledWith('/instalaciones');
  });

  it('crea una instalación con el cuerpo recibido', async () => {
    const datos = { nombre: 'Biblioteca', tipo: 'OTRA' as const, ubicacion: null, capacidad: 30, estado: 'ACTIVA' as const, descripcion: null };

    await instalacionesService.crear(datos);

    expect(post).toHaveBeenCalledWith('/instalaciones', datos);
  });

  it('modifica una instalación por id', async () => {
    const datos = { nombre: 'Comedor', tipo: 'COMEDOR' as const, ubicacion: null, capacidad: null, estado: 'EN_MANTENIMIENTO' as const, descripcion: null };

    await instalacionesService.modificar(6, datos);

    expect(put).toHaveBeenCalledWith('/instalaciones/6', datos);
  });
});
