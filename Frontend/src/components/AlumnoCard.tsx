import React from 'react';
import {
  Card,
  CardContent,
  Avatar,
  Box,
  Typography,
  Chip,
  Divider,
} from '@mui/material';
import {
  Email,
  Phone,
  LocationOn,
} from '@mui/icons-material';
import type { AlumnoDTO } from '../services/alumnoService';

interface AlumnoCardProps {
  alumno: AlumnoDTO;
}

export const AlumnoCard: React.FC<AlumnoCardProps> = ({ alumno }) => {
  const getStatusColor = (estado: string) => {
    switch (estado) {
      case 'ACTIVO':
        return 'success';
      case 'INACTIVO':
        return 'error';
      default:
        return 'default';
    }
  };

  const getNivelColor = (nivel: string) => {
    switch (nivel) {
      case 'INICIAL':
        return '#FF6B6B';
      case 'PRIMARIO':
        return '#4ECDC4';
      case 'SECUNDARIO':
        return '#45B7D1';
      default:
        return '#95E1D3';
    }
  };

  const initials = `${alumno.nombre[0]}${alumno.apellido[0]}`.toUpperCase();

  return (
    <Card
      sx={{
        maxWidth: 500,
        margin: '0 auto',
        marginTop: 3,
        boxShadow: '0 4px 20px rgba(0,0,0,0.1)',
        borderRadius: 2,
      }}
    >
      {/* Header */}
      <Box
        sx={{
          display: 'flex',
          alignItems: 'flex-start',
          gap: 2,
          padding: 2,
          borderBottom: '1px solid #eee',
        }}
      >
        <Avatar
          sx={{
            width: 56,
            height: 56,
            background: getNivelColor(alumno.nivelNombre),
            fontSize: '1.3rem',
            fontWeight: 'bold',
          }}
        >
          {initials}
        </Avatar>
        <Box sx={{ flex: 1 }}>
          <Typography variant="h6" sx={{ fontWeight: 600 }}>
            {alumno.nombre} {alumno.apellido}
          </Typography>
          <Box sx={{ marginTop: 1, display: 'flex', gap: 1 }}>
            <Chip
              label={alumno.estado}
              size="small"
              color={getStatusColor(alumno.estado)}
              variant="outlined"
            />
            <Chip
              label={alumno.nivelNombre}
              size="small"
              sx={{ backgroundColor: getNivelColor(alumno.nivelNombre), color: 'white' }}
            />
          </Box>
        </Box>
      </Box>

      {/* Content */}
      <CardContent>
        {/* Sección Académica */}
        <Box sx={{ marginBottom: 2.5 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#667eea', marginBottom: 1 }}>
            📚 Académica
          </Typography>
          <Typography variant="body2" sx={{ marginBottom: 0.5 }}>
            <strong>Curso:</strong> {alumno.cursoDescripcion}
          </Typography>
          <Typography variant="body2">
            <strong>Legajo:</strong> {alumno.legajo}
          </Typography>
        </Box>

        <Divider />

        {/* Sección Personal */}
        <Box sx={{ marginY: 2.5 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#764ba2', marginBottom: 1 }}>
            👤 Personal
          </Typography>
          <Typography variant="body2" sx={{ marginBottom: 0.5 }}>
            <strong>DNI:</strong> {alumno.dni}
          </Typography>
          <Typography variant="body2">
            <strong>Fecha de Nacimiento:</strong> {alumno.fechaNacimiento}
          </Typography>
        </Box>

        <Divider />

        {/* Sección Contacto */}
        <Box sx={{ marginTop: 2.5 }}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600, color: '#4ECDC4', marginBottom: 1 }}>
            📞 Contacto
          </Typography>
          {alumno.email && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, marginBottom: 0.8 }}>
              <Email sx={{ fontSize: 18, color: '#999' }} />
              <Typography variant="body2">{alumno.email}</Typography>
            </Box>
          )}
          {alumno.telefono && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, marginBottom: 0.8 }}>
              <Phone sx={{ fontSize: 18, color: '#999' }} />
              <Typography variant="body2">{alumno.telefono}</Typography>
            </Box>
          )}
          {alumno.domicilio && (
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <LocationOn sx={{ fontSize: 18, color: '#999' }} />
              <Typography variant="body2">{alumno.domicilio}</Typography>
            </Box>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};
