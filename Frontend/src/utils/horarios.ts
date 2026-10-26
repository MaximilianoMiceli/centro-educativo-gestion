import { PALETA } from '../institucion';

export const DIAS_NOMBRE = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];

export interface HorarioGrilla {
  id: number;
  materia: string;
  curso: string;
  docente: string;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
}

export interface BloqueHorario {
  id: number;
  dia: number;
  inicioMin: number;
  finMin: number;
  materia: string;
  curso: string;
  docente: string;
  color: string;
}

export interface GrillaHorarios {
  dias: number[];
  horaInicio: number;
  horaFin: number;
  bloques: BloqueHorario[];
}

const COLORES_MATERIAS = [
  PALETA.azulOscuro,
  PALETA.verde,
  PALETA.magenta,
  PALETA.naranja,
  PALETA.purpura,
  PALETA.azulClaro,
  PALETA.azul,
];

const HORA_MINIMA_GRILLA = 7;
const HORA_MAXIMA_GRILLA = 19;

/** Convierte "08:30" o "08:30:00" en minutos desde medianoche. */
export function minutosDeHora(hora: string): number {
  const coincidencia = /^(\d{1,2}):(\d{2})/.exec(hora);
  if (!coincidencia) {
    throw new Error(`Hora inválida: ${hora}`);
  }
  return Number(coincidencia[1]) * 60 + Number(coincidencia[2]);
}

/** Color estable por materia: la misma materia siempre se ve del mismo color. */
export function colorDeMateria(materia: string): string {
  let suma = 0;
  for (const caracter of materia) {
    suma += caracter.charCodeAt(0);
  }
  return COLORES_MATERIAS[suma % COLORES_MATERIAS.length];
}

/**
 * Arma la grilla: días a mostrar (lunes a viernes, sábado o domingo solo si hay clases),
 * rango horario y un bloque por horario. Los horarios fuera del rango amplían la grilla.
 */
export function armarGrilla(horarios: HorarioGrilla[]): GrillaHorarios {
  const bloques: BloqueHorario[] = horarios.map((h) => ({
    id: h.id,
    dia: h.diaSemana,
    inicioMin: minutosDeHora(h.horaInicio),
    finMin: minutosDeHora(h.horaFin),
    materia: h.materia,
    curso: h.curso,
    docente: h.docente,
    color: colorDeMateria(h.materia),
  }));

  const diasConClases = new Set(bloques.map((b) => b.dia));
  const dias = [1, 2, 3, 4, 5];
  if (diasConClases.has(6)) dias.push(6);
  if (diasConClases.has(7)) dias.push(7);

  const horaMasTemprana = bloques.length ? Math.floor(Math.min(...bloques.map((b) => b.inicioMin)) / 60) : HORA_MINIMA_GRILLA;
  const horaMasTarde = bloques.length ? Math.ceil(Math.max(...bloques.map((b) => b.finMin)) / 60) : HORA_MAXIMA_GRILLA;

  return {
    dias,
    horaInicio: Math.min(HORA_MINIMA_GRILLA, horaMasTemprana),
    horaFin: Math.max(HORA_MAXIMA_GRILLA, horaMasTarde),
    bloques,
  };
}
