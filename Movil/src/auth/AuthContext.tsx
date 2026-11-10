import React, { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { iniciarSesion } from '../api/backend';
import { ApiError, alSesionExpirada, fijarToken } from '../api/cliente';
import type { Sesion } from '../api/tipos';
import { ROLES_PERMITIDOS } from '../config';
import { estaVencido, leerToken } from '../utils/jwt';
import { borrarSesion, guardarSesion, leerSesionGuardada } from './almacen';

interface ValorAuth {
  sesion: Sesion | null;
  cargando: boolean;
  iniciar: (username: string, password: string) => Promise<void>;
  cerrar: () => Promise<void>;
  /** Reemplaza la sesión, por ejemplo después de cambiar la contraseña, que devuelve un token nuevo. */
  actualizar: (sesion: Sesion) => Promise<void>;
}

const AuthContext = createContext<ValorAuth | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(null);
  const [cargando, setCargando] = useState(true);

  const cerrar = useCallback(async () => {
    fijarToken(null);
    await borrarSesion();
    setSesion(null);
  }, []);

  const actualizar = useCallback(async (nueva: Sesion) => {
    fijarToken(nueva.token);
    await guardarSesion(nueva);
    setSesion(nueva);
  }, []);

  useEffect(() => {
    alSesionExpirada(() => {
      void cerrar();
    });
    (async () => {
      const guardada = await leerSesionGuardada();
      if (guardada && !estaVencido(leerToken(guardada.token))) {
        fijarToken(guardada.token);
        setSesion(guardada);
      } else if (guardada) {
        await borrarSesion();
      }
      setCargando(false);
    })().catch(async () => {
      await borrarSesion();
      setCargando(false);
    });
    return () => alSesionExpirada(null);
  }, [cerrar]);

  const iniciar = useCallback(
    async (username: string, password: string) => {
      const respuesta = await iniciarSesion(username.trim(), password);
      if (!ROLES_PERMITIDOS.includes(respuesta.rol)) {
        throw new ApiError(403, 'Esta aplicación es para padres y alumnos. Usá el sistema web para tu perfil.');
      }
      await actualizar({
        token: respuesta.token,
        username: respuesta.username,
        rol: respuesta.rol,
        debeCambiarPassword: respuesta.debeCambiarPassword,
      });
    },
    [actualizar],
  );

  const valor = useMemo(
    () => ({ sesion, cargando, iniciar, cerrar, actualizar }),
    [sesion, cargando, iniciar, cerrar, actualizar],
  );
  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth(): ValorAuth {
  const valor = useContext(AuthContext);
  if (!valor) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }
  return valor;
}
