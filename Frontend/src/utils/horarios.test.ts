import { describe, expect, it } from 'vitest';
import { armarGrilla, colorDeMateria, minutosDeHora } from './horarios';
import type { HorarioGrilla } from './horarios';

const clase = (id: number, diaSemana: number, horaInicio: string, horaFin: string, materia = 'Matemática'): HorarioGrilla => ({
  id,
  materia,
  curso: '1° A',
  docente: 'López, María',
  diaSemana,
  horaInicio,
  horaFin,
});

describe('minutosDeHora', () => {
  it('acepta horas con o sin segundos', () => {
    expect(minutosDeHora('08:30')).toBe(510);
    expect(minutosDeHora('08:30:00')).toBe(510);
  });

  it('rechaza textos que no son horas', () => {
    expect(() => minutosDeHora('mediodía')).toThrow();
  });
});

describe('armarGrilla', () => {
  it('muestra lunes a viernes cuando no hay clases en fin de semana', () => {
    const grilla = armarGrilla([clase(1, 1, '08:00', '09:30')]);
    expect(grilla.dias).toEqual([1, 2, 3, 4, 5]);
  });

  it('agrega el sábado solo si hay clases ese día', () => {
    const grilla = armarGrilla([clase(1, 1, '08:00', '09:30'), clase(2, 6, '09:00', '10:00')]);
    expect(grilla.dias).toEqual([1, 2, 3, 4, 5, 6]);
  });

  it('ubica cada bloque en minutos desde la medianoche', () => {
    const grilla = armarGrilla([clase(1, 2, '14:00', '15:30')]);
    expect(grilla.bloques[0]).toMatchObject({ dia: 2, inicioMin: 840, finMin: 930 });
  });

  it('amplía el rango horario si una clase cae fuera del rango por defecto', () => {
    const grilla = armarGrilla([clase(1, 1, '06:00', '07:00'), clase(2, 1, '20:00', '21:30')]);
    expect(grilla.horaInicio).toBe(6);
    expect(grilla.horaFin).toBe(22);
  });

  it('usa el rango por defecto cuando no hay horarios', () => {
    const grilla = armarGrilla([]);
    expect(grilla.horaInicio).toBe(7);
    expect(grilla.horaFin).toBe(19);
    expect(grilla.bloques).toEqual([]);
  });
});

describe('colorDeMateria', () => {
  it('asigna siempre el mismo color a la misma materia', () => {
    expect(colorDeMateria('Lengua')).toBe(colorDeMateria('Lengua'));
  });
});
