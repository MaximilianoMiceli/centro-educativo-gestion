import React from 'react';
import { TablePagination } from '@mui/material';
import { OPCIONES_POR_PAGINA } from '../utils/paginacion';

interface PaginadorTablaProps {
  total: number;
  pagina: number;
  porPagina: number;
  onPaginaChange: (pagina: number) => void;
  onPorPaginaChange: (porPagina: number) => void;
}

/** Paginador de MUI para listas de tablas. La página es 0-based, como en MUI. */
export const PaginadorTabla: React.FC<PaginadorTablaProps> = ({
  total,
  pagina,
  porPagina,
  onPaginaChange,
  onPorPaginaChange,
}) => (
  <TablePagination
    component="div"
    count={total}
    page={pagina}
    rowsPerPage={porPagina}
    rowsPerPageOptions={OPCIONES_POR_PAGINA}
    onPageChange={(_evento, nueva) => onPaginaChange(nueva)}
    onRowsPerPageChange={(evento) => onPorPaginaChange(Number(evento.target.value))}
    labelRowsPerPage="Filas por página"
    labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
  />
);
