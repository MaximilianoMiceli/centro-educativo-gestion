import React from 'react';
import { Skeleton, TableCell, TableRow } from '@mui/material';

interface FilasCargandoProps {
  columnas: number;
  filas?: number;
}

/** Filas de esqueleto mientras una tabla carga, para que la pantalla no salte al llegar los datos. */
export const FilasCargando: React.FC<FilasCargandoProps> = ({ columnas, filas = 5 }) => (
  <>
    {Array.from({ length: filas }, (_, indice) => (
      <TableRow key={indice}>
        {Array.from({ length: columnas }, (_c, columna) => (
          <TableCell key={columna}>
            <Skeleton variant="text" />
          </TableCell>
        ))}
      </TableRow>
    ))}
  </>
);
