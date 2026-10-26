import { describe, expect, it } from 'vitest';
import { render, screen } from '@testing-library/react';
import { HorarioSemanal } from './HorarioSemanal';
import { EstadoVacio } from './EstadoVacio';

describe('HorarioSemanal', () => {
  it('muestra cada clase con su materia, curso y día', () => {
    render(
      <HorarioSemanal
        horarios={[
          { id: 1, materia: 'Matemática', curso: '1° A', docente: 'López, María', diaSemana: 1, horaInicio: '08:00:00', horaFin: '09:30:00' },
          { id: 2, materia: 'Lengua', curso: '1° A', docente: 'López, María', diaSemana: 3, horaInicio: '10:00', horaFin: '11:00' },
        ]}
        mostrarDocente
      />,
    );
    expect(screen.getByText('Lunes')).toBeTruthy();
    expect(screen.getByText('Miércoles')).toBeTruthy();
    expect(screen.getAllByText('1° A').length).toBe(2);
    expect(screen.getAllByText('López, María').length).toBe(2);
  });

  it('avisa cuando no hay horarios', () => {
    render(<HorarioSemanal horarios={[]} />);
    expect(screen.getByText('No hay horarios cargados.')).toBeTruthy();
  });
});

describe('EstadoVacio', () => {
  it('muestra el título, la descripción y la acción', () => {
    render(<EstadoVacio titulo="Sin datos" descripcion="Cargá algo para empezar" accion={<button>Crear</button>} />);
    expect(screen.getByText('Sin datos')).toBeTruthy();
    expect(screen.getByText('Cargá algo para empezar')).toBeTruthy();
    expect(screen.getByRole('button', { name: 'Crear' })).toBeTruthy();
  });
});
