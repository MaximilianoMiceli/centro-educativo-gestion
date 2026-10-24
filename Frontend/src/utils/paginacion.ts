export const OPCIONES_POR_PAGINA = [10, 25, 50];

/** Devuelve la página pedida; si la lista se achicó, se queda en la última página disponible. */
export function paginar<T>(items: T[], pagina: number, porPagina: number): { visibles: T[]; paginaActual: number } {
  const ultimaPagina = Math.max(0, Math.ceil(items.length / porPagina) - 1);
  const paginaActual = Math.min(Math.max(pagina, 0), ultimaPagina);
  const inicio = paginaActual * porPagina;
  return { visibles: items.slice(inicio, inicio + porPagina), paginaActual };
}
