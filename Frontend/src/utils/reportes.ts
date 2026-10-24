export type FiltroReporte = 'alumno' | 'docente' | 'nivel' | 'curso' | 'materia' | 'deporte' | 'recorrido';

export interface ReglaReporte {
  filtros: FiltroReporte[];
  requeridos: FiltroReporte[];
}

/** Filtros que muestra cada reporte, indexados por el slug que expone el backend. */
export const REGLAS_REPORTES: Record<string, ReglaReporte> = {
  'alumno-ficha': { filtros: ['alumno'], requeridos: ['alumno'] },
  'docente-cursos': { filtros: ['docente', 'nivel'], requeridos: [] },
  'alumnos-curso': { filtros: ['nivel', 'curso'], requeridos: [] },
  'alumnos-materia': { filtros: ['nivel', 'curso', 'materia'], requeridos: [] },
  'docentes-nivel': { filtros: ['nivel'], requeridos: [] },
  'alumnos-deporte': { filtros: ['deporte'], requeridos: [] },
  'alumnos-deporte-nivel': { filtros: ['deporte', 'nivel'], requeridos: ['nivel'] },
  'deportes-detalle': { filtros: ['deporte', 'nivel'], requeridos: [] },
  'alumnos-transporte': { filtros: ['recorrido'], requeridos: [] },
};

export interface FiltrosSeleccionados {
  alumnoId?: number;
  docenteId?: number;
  nivelId?: number;
  cursoId?: number;
  materiaId?: number;
  deporteId?: number;
  recorridoId?: number;
}

export const CLAVE_FILTRO: Record<FiltroReporte, keyof FiltrosSeleccionados> = {
  alumno: 'alumnoId',
  docente: 'docenteId',
  nivel: 'nivelId',
  curso: 'cursoId',
  materia: 'materiaId',
  deporte: 'deporteId',
  recorrido: 'recorridoId',
};

export function faltantes(slug: string, filtros: FiltrosSeleccionados): FiltroReporte[] {
  const regla = REGLAS_REPORTES[slug];
  if (!regla) return [];
  return regla.requeridos.filter((filtro) => filtros[CLAVE_FILTRO[filtro]] === undefined);
}

export function paramsDe(filtros: FiltrosSeleccionados): Record<string, number> {
  const params: Record<string, number> = {};
  for (const [clave, valor] of Object.entries(filtros)) {
    if (valor !== undefined && valor !== null) params[clave] = valor;
  }
  return params;
}

export function nombreDesdeContentDisposition(header: string | undefined): string | null {
  const coincidencia = header?.match(/filename="?([^";]+)"?/i);
  return coincidencia ? coincidencia[1] : null;
}
