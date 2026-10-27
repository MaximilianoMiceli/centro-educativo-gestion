import { describe, expect, it } from 'vitest';
import { agruparMenu, esSeccionDatos, itemsPermitidos, puedeVer, rutaInicial } from './navegacion';

describe('itemsPermitidos', () => {
  it('el administrador ve todo el menú de gestión', () => {
    const rutas = itemsPermitidos('ADMIN').map((i) => i.ruta);
    expect(rutas).toContain('/usuarios');
    expect(rutas).toContain('/datos/instalaciones');
    expect(rutas).not.toContain('/mi-informacion');
  });

  it('la dirección ve inicio, alumnos y reportes, pero no la administración', () => {
    const rutas = itemsPermitidos('DIRECCION').map((i) => i.ruta);
    expect(rutas).toEqual(['/inicio', '/alumnos', '/horarios', '/servicios', '/reportes']);
  });

  it('el profesor solo ve alumnos', () => {
    expect(itemsPermitidos('PROFESOR').map((i) => i.ruta)).toEqual(['/mi-agenda', '/alumnos', '/calificaciones']);
  });

  it('padres y alumnos solo ven su información', () => {
    expect(itemsPermitidos('PADRE').map((i) => i.ruta)).toEqual(['/mi-informacion', '/mis-calificaciones', '/mis-servicios']);
    expect(itemsPermitidos('ALUMNO').map((i) => i.ruta)).toEqual(['/mi-informacion', '/mis-calificaciones', '/mis-servicios']);
  });

  it('sin rol no hay accesos', () => {
    expect(itemsPermitidos(null)).toEqual([]);
  });
});

describe('puedeVer', () => {
  it('bloquea las rutas de otro rol', () => {
    expect(puedeVer('PROFESOR', '/usuarios')).toBe(false);
    expect(puedeVer('ADMIN', '/usuarios')).toBe(true);
  });
});

describe('rutaInicial', () => {
  it('cada rol aterriza en su pantalla principal', () => {
    expect(rutaInicial('ADMIN')).toBe('/inicio');
    expect(rutaInicial('DIRECCION')).toBe('/inicio');
    expect(rutaInicial('PROFESOR')).toBe('/alumnos');
    expect(rutaInicial('PADRE')).toBe('/mi-informacion');
    expect(rutaInicial('ALUMNO')).toBe('/mi-informacion');
  });
});

describe('agruparMenu', () => {
  it('agrupa por encabezado manteniendo el orden de aparición', () => {
    const grupos = agruparMenu(itemsPermitidos('ADMIN'));
    expect(grupos.map((g) => g.titulo)).toEqual(['', 'Personas', 'Académico', 'Servicios', 'Administración']);
    expect(grupos[0].items.map((i) => i.etiqueta)).toEqual(['Inicio', 'Reportes']);
  });
});

describe('esSeccionDatos', () => {
  it('acepta solo las secciones de datos conocidas', () => {
    expect(esSeccionDatos('instalaciones')).toBe(true);
    expect(esSeccionDatos('usuarios')).toBe(false);
    expect(esSeccionDatos(undefined)).toBe(false);
  });
});
