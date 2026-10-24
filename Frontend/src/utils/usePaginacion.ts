import { useState } from 'react';
import { OPCIONES_POR_PAGINA, paginar } from './paginacion';

/** Estado de paginado para una lista ya cargada en memoria. */
export function usePaginacion<T>(items: T[], porPaginaInicial = OPCIONES_POR_PAGINA[0]) {
  const [pagina, setPagina] = useState(0);
  const [porPagina, setPorPagina] = useState(porPaginaInicial);
  const { visibles, paginaActual } = paginar(items, pagina, porPagina);

  return {
    visibles,
    total: items.length,
    pagina: paginaActual,
    porPagina,
    setPagina,
    cambiarPorPagina: (nueva: number) => {
      setPorPagina(nueva);
      setPagina(0);
    },
  };
}
