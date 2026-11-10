const MESES = [
  'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
  'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre',
];

/** Importe en pesos con el formato argentino: $ 1.234,50. Se arma a mano porque no todos los motores traen Intl. */
export function moneda(valor: number): string {
  const negativo = valor < 0;
  const [entero, decimales] = Math.abs(valor).toFixed(2).split('.');
  const conPuntos = entero.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${negativo ? '-' : ''}$ ${conPuntos},${decimales}`;
}

/** De AAAA-MM-DD a DD/MM/AAAA. */
export function fecha(iso: string): string {
  const [anio, mes, dia] = iso.slice(0, 10).split('-');
  return `${dia}/${mes}/${anio}`;
}

/** De AAAA-MM a "Diciembre 2026". */
export function periodoLegible(periodo: string): string {
  const [anio, mes] = periodo.split('-');
  return `${MESES[Number(mes) - 1] ?? mes} ${anio}`;
}

export function etiquetaEstado(estado: string): string {
  switch (estado) {
    case 'PENDIENTE':
      return 'Pendiente';
    case 'PARCIAL':
      return 'Pago parcial';
    case 'PAGADA':
    case 'PAGADO':
      return 'Pagada';
    case 'VENCIDO':
      return 'Vencida';
    default:
      return estado;
  }
}

export function etiquetaConcepto(concepto: string): string {
  switch (concepto) {
    case 'CUOTA_BASE':
      return 'Cuota';
    case 'DEPORTE':
      return 'Deporte';
    case 'TRANSPORTE':
      return 'Transporte';
    case 'COMEDOR':
      return 'Comedor';
    default:
      return concepto;
  }
}
