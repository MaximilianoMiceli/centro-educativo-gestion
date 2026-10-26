import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import { ConfirmDialog } from './ConfirmDialog';

describe('ConfirmDialog', () => {
  it('muestra el título y el mensaje cuando está abierto', () => {
    render(
      <ConfirmDialog abierto titulo="Dar de baja el deporte" mensaje="¿Seguro?" onConfirmar={() => {}} onCancelar={() => {}} />,
    );
    expect(screen.getByText('Dar de baja el deporte')).toBeTruthy();
    expect(screen.getByText('¿Seguro?')).toBeTruthy();
  });

  it('ejecuta la acción solo al confirmar', () => {
    const onConfirmar = vi.fn();
    const onCancelar = vi.fn();
    render(
      <ConfirmDialog abierto titulo="Desactivar" mensaje="Ya no podrá ingresar" textoConfirmar="Desactivar" peligro onConfirmar={onConfirmar} onCancelar={onCancelar} />,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Desactivar' }));
    expect(onConfirmar).toHaveBeenCalledTimes(1);
    expect(onCancelar).not.toHaveBeenCalled();
  });

  it('cancelar no ejecuta la acción', () => {
    const onConfirmar = vi.fn();
    const onCancelar = vi.fn();
    render(
      <ConfirmDialog abierto titulo="Dar de baja" mensaje="¿Seguro?" onConfirmar={onConfirmar} onCancelar={onCancelar} />,
    );
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }));
    expect(onCancelar).toHaveBeenCalledTimes(1);
    expect(onConfirmar).not.toHaveBeenCalled();
  });
});
