import React, { useState } from 'react';
import {
  Box,
  Button,
  TextField,
  CircularProgress,
  Alert,
  Card,
  CardContent,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
  InputAdornment,
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { alumnoService } from '../services/alumnoService';
import type { AlumnoDTO } from '../services/alumnoService';
import { AlumnoCard } from './AlumnoCard';

interface AlumnoSearchProps {
  token: string;
}

type SearchType = 'id' | 'legajo' | 'dni' | 'nombre';

export const AlumnoSearch: React.FC<AlumnoSearchProps> = ({ token }) => {
  const [searchType, setSearchType] = useState<SearchType>('legajo');
  const [searchValue, setSearchValue] = useState('');
  const [resultado, setResultado] = useState<AlumnoDTO | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState('');

  alumnoService.setToken(token);

  const buscar = async () => {
    if (!searchValue.trim()) {
      setError('Ingresa un valor para buscar');
      return;
    }

    setCargando(true);
    setError('');
    setResultado(null);

    try {
      let data: AlumnoDTO;

      switch (searchType) {
        case 'id':
          data = await alumnoService.obtenerPorId(parseInt(searchValue));
          break;
        case 'legajo':
          data = await alumnoService.buscarPorLegajo(searchValue);
          break;
        case 'dni':
          data = await alumnoService.buscarPorDni(searchValue);
          break;
        case 'nombre':
          data = await alumnoService.buscarPorNombre(searchValue);
          break;
      }

      setResultado(data);
    } catch (err: any) {
      const errorMsg = err.response?.data?.message || 'No se encontró el alumno';
      setError(errorMsg);
    } finally {
      setCargando(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      buscar();
    }
  };

  const handleSearchTypeChange = (_event: React.MouseEvent<HTMLElement>, newType: SearchType) => {
    if (newType !== null) {
      setSearchType(newType);
      setSearchValue('');
      setResultado(null);
      setError('');
    }
  };

  const getPlaceholder = () => {
    switch (searchType) {
      case 'id':
        return 'Ej: 1';
      case 'legajo':
        return 'Ej: ALU001';
      case 'dni':
        return 'Ej: 45678901';
      case 'nombre':
        return 'Ej: Carlos García';
      default:
        return '';
    }
  };


  return (
    <Box sx={{ padding: 3, maxWidth: 600, margin: '0 auto' }}>
      <Card sx={{ marginBottom: 3, boxShadow: '0 2px 8px rgba(0,0,0,0.08)' }}>
        <CardContent>
          <Typography variant="h5" sx={{ marginBottom: 2, fontWeight: 600 }}>
            🔍 Buscar Alumno
          </Typography>

          <Box sx={{ marginBottom: 2 }}>
            <Typography variant="caption" sx={{ color: '#666', fontWeight: 500 }}>
              Tipo de búsqueda
            </Typography>
            <ToggleButtonGroup
              value={searchType}
              exclusive
              onChange={handleSearchTypeChange}
              fullWidth
              sx={{ marginTop: 1 }}
            >
              <ToggleButton value="id" sx={{ fontSize: '0.85rem' }}>
                ID
              </ToggleButton>
              <ToggleButton value="legajo" sx={{ fontSize: '0.85rem' }}>
                Legajo
              </ToggleButton>
              <ToggleButton value="dni" sx={{ fontSize: '0.85rem' }}>
                DNI
              </ToggleButton>
              <ToggleButton value="nombre" sx={{ fontSize: '0.85rem' }}>
                Nombre
              </ToggleButton>
            </ToggleButtonGroup>
          </Box>

          {/* Campo de búsqueda */}
          <TextField
            fullWidth
            placeholder={getPlaceholder()}
            value={searchValue}
            onChange={(e) => setSearchValue(e.target.value)}
            onKeyPress={handleKeyPress}
            disabled={cargando}
            variant="outlined"
            size="small"
            sx={{ marginBottom: 2 }}
            slotProps={{
              input: {
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: '#999' }} />
                  </InputAdornment>
                ),
              },
            }}
          />

          {/* Botón de búsqueda */}
          <Button
            variant="contained"
            fullWidth
            onClick={buscar}
            disabled={cargando || !searchValue.trim()}
            sx={{
              background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
              padding: '10px',
              fontSize: '1rem',
              fontWeight: 600,
            }}
          >
            {cargando ? <CircularProgress size={24} color="inherit" /> : 'Buscar'}
          </Button>
        </CardContent>
      </Card>

      {/* Mensajes de error */}
      {error && (
        <Alert
          severity="error"
          sx={{ marginBottom: 2 }}
          onClose={() => setError('')}
        >
          {error}
        </Alert>
      )}

      {/* Resultado */}
      {resultado && <AlumnoCard alumno={resultado} />}

      {/* Datos de prueba */}
      {!resultado && !error && (
        <Card sx={{ marginTop: 3, backgroundColor: '#F5F5F5', boxShadow: 'none' }}>
          <CardContent>
            <Typography variant="subtitle2" sx={{ fontWeight: 600, marginBottom: 1 }}>
              📋 Datos de Prueba Disponibles
            </Typography>
            <Typography variant="caption" sx={{ display: 'block', marginBottom: 0.5 }}>
              <strong>Legajos:</strong> ALU001, ALU002, ALU003, ALU004
            </Typography>
            <Typography variant="caption" sx={{ display: 'block', marginBottom: 0.5 }}>
              <strong>DNIs:</strong> 45678901, 56789012, 67890123, 78901234
            </Typography>
            <Typography variant="caption" sx={{ display: 'block' }}>
              <strong>IDs:</strong> 1, 2, 3, 4
            </Typography>
          </CardContent>
        </Card>
      )}
    </Box>
  );
};
