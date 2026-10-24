import { describe, expect, it } from 'vitest';
import { faltantes, nombreDesdeContentDisposition, paramsDe } from './reportes';

describe('faltantes', () => {
  it('pide el alumno para la ficha hasta que se elige uno', () => {
    expect(faltantes('alumno-ficha', {})).toEqual(['alumno']);
    expect(faltantes('alumno-ficha', { alumnoId: 1 })).toEqual([]);
  });

  it('pide el nivel para el reporte de deportes por nivel', () => {
    expect(faltantes('alumnos-deporte-nivel', { deporteId: 2 })).toEqual(['nivel']);
  });

  it('no exige filtros a los reportes de listado', () => {
    expect(faltantes('alumnos-curso', {})).toEqual([]);
  });
});

describe('paramsDe', () => {
  it('omite los filtros sin valor', () => {
    expect(paramsDe({ nivelId: 2, cursoId: undefined, deporteId: undefined })).toEqual({ nivelId: 2 });
  });
});

describe('nombreDesdeContentDisposition', () => {
  it('lee el nombre del archivo del header', () => {
    expect(nombreDesdeContentDisposition('attachment; filename=REP-003-alumnos-curso.pdf')).toBe(
      'REP-003-alumnos-curso.pdf',
    );
    expect(nombreDesdeContentDisposition('attachment; filename="REP-005-docentes-nivel.xlsx"')).toBe(
      'REP-005-docentes-nivel.xlsx',
    );
  });

  it('devuelve null si no hay nombre', () => {
    expect(nombreDesdeContentDisposition(undefined)).toBeNull();
  });
});
