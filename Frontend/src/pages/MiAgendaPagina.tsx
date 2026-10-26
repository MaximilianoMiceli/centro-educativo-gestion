import React, { useEffect, useState } from 'react';
import { Alert, Box, Card, CardContent, Chip, CircularProgress, Divider, Stack, Typography } from '@mui/material';
import { agendaService } from '../services/agendaService';
import type { DocenteAgenda } from '../services/docenteService';
import { horariosService } from '../services/horariosService';
import type { HorarioDTO } from '../services/horariosService';
import { HorarioSemanal } from '../components/HorarioSemanal';
import { EstadoVacio } from '../components/EstadoVacio';
import { PALETA } from '../institucion';

interface MiAgendaPaginaProps {
  token: string;
}

/** Vista del profesor: sus cursos, materias y horario semanal. */
export const MiAgendaPagina: React.FC<MiAgendaPaginaProps> = ({ token }) => {
  const [agenda, setAgenda] = useState<DocenteAgenda | null>(null);
  const [horarios, setHorarios] = useState<HorarioDTO[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    agendaService.setToken(token);
    horariosService.setToken(token);
    setCargando(true);
    agendaService
      .miAgenda()
      .then((miAgenda) => {
        setAgenda(miAgenda);
        return horariosService.listar({ docenteId: miAgenda.docenteId });
      })
      .then(setHorarios)
      .catch(() => setError('No pudimos cargar tu agenda. Si el problema continúa, avisá a administración.'))
      .finally(() => setCargando(false));
  }, [token]);

  if (cargando) {
    return <Box sx={{ display: 'flex', justifyContent: 'center', py: 8 }}><CircularProgress /></Box>;
  }

  if (error) {
    return <Alert severity="error">{error}</Alert>;
  }

  if (!agenda) {
    return (
      <EstadoVacio
        titulo="Tu usuario todavía no está vinculado a un docente"
        descripcion="Pedile a administración que vincule tu cuenta con tu ficha docente para ver tus cursos y horarios."
      />
    );
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ fontWeight: 700 }}>Mi agenda</Typography>
      <Typography variant="body2" sx={{ color: '#666', mb: 3 }}>{agenda.docente}</Typography>

      <Card sx={{ mb: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 1.5, color: PALETA.azul }}>Mis cursos</Typography>
          {agenda.cursos.length === 0 && (
            <EstadoVacio titulo="Todavía no tenés cursos asignados" />
          )}
          {agenda.cursos.map((curso, indice) => (
            <Box key={curso.cursoId} sx={{ mb: 2 }}>
              {indice > 0 && <Divider sx={{ mb: 2 }} />}
              <Stack direction="row" spacing={1} sx={{ alignItems: 'center', mb: 1 }}>
                <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>{curso.curso}</Typography>
                <Chip size="small" label={curso.nivel} variant="outlined" />
              </Stack>
              {curso.materias.map((materia) => (
                <Box key={materia.materia} sx={{ pl: 2, mb: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>{materia.materia}</Typography>
                  <Typography variant="body2" sx={{ color: '#555' }}>
                    {materia.horarios.length ? materia.horarios.join(' · ') : 'Sin horarios cargados'}
                  </Typography>
                </Box>
              ))}
            </Box>
          ))}
        </CardContent>
      </Card>

      <Card sx={{ boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2, color: PALETA.azul }}>Horario semanal</Typography>
          <HorarioSemanal horarios={horarios} mostrarDocente={false} />
        </CardContent>
      </Card>
    </Box>
  );
};
