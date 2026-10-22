import { beforeEach, describe, expect, it, vi } from 'vitest';

const { get } = vi.hoisted(() => ({ get: vi.fn() }));

vi.mock('axios', () => ({
  default: {
    create: () => ({ defaults: { headers: { common: {} } }, get }),
  },
}));

import { alumnosAdminService } from './alumnosAdminService';

describe('alumnosAdminService.listar', () => {
  beforeEach(() => {
    get.mockReset();
    get.mockResolvedValue({ data: [] });
  });

  it('envía el nivel además del curso y la búsqueda', async () => {
    await alumnosAdminService.listar({ busqueda: 'ana', nivelId: 2, cursoId: 5 });

    expect(get).toHaveBeenCalledWith('/alumnos', { params: { busqueda: 'ana', nivelId: 2, cursoId: 5 } });
  });

  it('no envía filtros vacíos', async () => {
    await alumnosAdminService.listar({ busqueda: '' });

    expect(get).toHaveBeenCalledWith('/alumnos', { params: {} });
  });
});
