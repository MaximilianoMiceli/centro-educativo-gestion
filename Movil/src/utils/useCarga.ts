import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../api/cliente';

export interface EstadoCarga<T> {
  datos: T | null;
  cargando: boolean;
  error: string | null;
  recargar: () => void;
}

/** Ejecuta una carga cada vez que cambian las dependencias y deja disponible el estado y un botón de recarga. */
export function useCarga<T>(cargar: (() => Promise<T>) | null, dependencias: unknown[]): EstadoCarga<T> {
  const [datos, setDatos] = useState<T | null>(null);
  const [cargando, setCargando] = useState(cargar !== null);
  const [error, setError] = useState<string | null>(null);
  const [version, setVersion] = useState(0);
  const ultima = useRef(0);

  useEffect(() => {
    if (!cargar) {
      setCargando(false);
      return;
    }
    const numero = ++ultima.current;
    setCargando(true);
    setError(null);
    cargar()
      .then((resultado) => {
        if (numero === ultima.current) {
          setDatos(resultado);
        }
      })
      .catch((e: unknown) => {
        if (numero === ultima.current) {
          setError(e instanceof ApiError ? e.message : 'Ocurrió un error inesperado');
        }
      })
      .finally(() => {
        if (numero === ultima.current) {
          setCargando(false);
        }
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependencias, version]);

  const recargar = useCallback(() => setVersion((v) => v + 1), []);
  return { datos, cargando, error, recargar };
}
