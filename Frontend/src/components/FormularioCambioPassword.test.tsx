import { cleanup, fireEvent, render, screen } from '@testing-library/react';
import { afterEach, describe, expect, it, vi } from 'vitest';
import { FormularioCambioPassword } from './CambiarPassword';

afterEach(cleanup);

describe('FormularioCambioPassword', () => {
  it('no permite guardar hasta completar los tres campos', () => {
    render(<FormularioCambioPassword token="t" onCambiado={vi.fn()} />);

    const boton = screen.getByRole('button', { name: /guardar nueva contraseña/i });
    expect((boton as HTMLButtonElement).disabled).toBe(true);
  });

  it('avisa cuando la repetición no coincide', () => {
    render(<FormularioCambioPassword token="t" onCambiado={vi.fn()} />);

    fireEvent.change(screen.getByLabelText(/contraseña actual/i), { target: { value: 'vieja123' } });
    fireEvent.change(screen.getByLabelText(/^nueva contraseña/i), { target: { value: 'NuevaClave1' } });
    fireEvent.change(screen.getByLabelText(/repetir nueva contraseña/i), { target: { value: 'otra' } });

    expect(screen.getByText('Las contraseñas no coinciden')).toBeTruthy();
  });
});
