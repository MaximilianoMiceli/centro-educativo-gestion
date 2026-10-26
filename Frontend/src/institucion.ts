export const INSTITUCION = {
  nombre: 'Educar Para Transformar',
  subtitulo: 'Centro Educativo Privado',
  localidad: 'Resistencia, Chaco, Argentina',
} as const;

/** Colores tomados del logo oficial del centro. */
export const PALETA = {
  azulOscuro: '#094A98',
  azul: '#184782',
  azulClaro: '#6AADDA',
  azulSuave: '#E6F2FB',
  verde: '#82BD3B',
  magenta: '#E01585',
  purpura: '#8D2387',
  naranja: '#ED4F28',
  grisTexto: '#1F2A37',
  grisSuave: '#F4F8FC',
} as const;

export const GRADIENTE_PRINCIPAL = `linear-gradient(135deg, ${PALETA.azulOscuro} 0%, ${PALETA.azulClaro} 100%)`;

/** Fondo de la pantalla de ingreso: tonos suaves de los cuatro colores de las figuras del logo. */
export const FONDO_INGRESO = `linear-gradient(135deg, ${PALETA.azulSuave} 0%, #EEF8E6 50%, #FCE8F3 100%)`;
