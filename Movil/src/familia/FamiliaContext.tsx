import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { hijos as pedirHijos } from '../api/backend';
import type { Hijo } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { useCarga } from '../utils/useCarga';

interface ValorFamilia {
  hijos: Hijo[];
  hijo: Hijo | null;
  elegir: (id: number) => void;
  cargando: boolean;
  error: string | null;
  recargar: () => void;
}

const FamiliaContext = createContext<ValorFamilia | null>(null);

/** Los hijos vinculados al padre, o el propio alumno, y cuál de ellos está seleccionado. */
export function FamiliaProvider({ children }: { children: React.ReactNode }) {
  const { sesion } = useAuth();
  const activa = sesion !== null && !sesion.debeCambiarPassword;
  const { datos, cargando, error, recargar } = useCarga(activa ? pedirHijos : null, [activa]);
  const [elegido, setElegido] = useState<number | null>(null);

  const hijos = datos ?? [];
  useEffect(() => {
    if (hijos.length > 0 && !hijos.some((h) => h.id === elegido)) {
      setElegido(hijos[0].id);
    }
  }, [hijos, elegido]);

  const valor = useMemo(
    () => ({
      hijos,
      hijo: hijos.find((h) => h.id === elegido) ?? null,
      elegir: setElegido,
      cargando,
      error,
      recargar,
    }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [datos, elegido, cargando, error, recargar],
  );
  return <FamiliaContext.Provider value={valor}>{children}</FamiliaContext.Provider>;
}

export function useFamilia(): ValorFamilia {
  const valor = useContext(FamiliaContext);
  if (!valor) {
    throw new Error('useFamilia debe usarse dentro de FamiliaProvider');
  }
  return valor;
}
