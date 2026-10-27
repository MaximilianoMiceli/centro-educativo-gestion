export const SECCIONES_DATOS = [
  'niveles',
  'cursos',
  'materias',
  'asignaciones',
  'deportes',
  'instalaciones',
  'transporte',
] as const;

export type SeccionDatos = (typeof SECCIONES_DATOS)[number];

export interface ItemMenu {
  ruta: string;
  etiqueta: string;
  /** Encabezado del grupo en el menú lateral. Vacío para los accesos sueltos. */
  grupo: string;
  roles: string[];
  /** Clave del ícono; el componente de layout la resuelve a un ícono de MUI. */
  icono: string;
}

const ADMIN = ['ADMIN'];
const GESTION = ['ADMIN', 'DIRECCION'];

export const ITEMS_MENU: ItemMenu[] = [
  { ruta: '/inicio', etiqueta: 'Inicio', grupo: '', roles: GESTION, icono: 'inicio' },
  { ruta: '/mi-agenda', etiqueta: 'Mi agenda', grupo: '', roles: ['PROFESOR'], icono: 'agenda' },
  { ruta: '/alumnos', etiqueta: 'Alumnos', grupo: 'Personas', roles: [...GESTION, 'PROFESOR'], icono: 'alumnos' },
  { ruta: '/docentes', etiqueta: 'Docentes', grupo: 'Personas', roles: ADMIN, icono: 'docentes' },
  { ruta: '/horarios', etiqueta: 'Horarios', grupo: 'Académico', roles: GESTION, icono: 'horarios' },
  { ruta: '/calificaciones', etiqueta: 'Calificaciones', grupo: 'Académico', roles: ['ADMIN', 'PROFESOR'], icono: 'calificaciones' },
  { ruta: '/datos/niveles', etiqueta: 'Niveles', grupo: 'Académico', roles: ADMIN, icono: 'niveles' },
  { ruta: '/datos/cursos', etiqueta: 'Cursos', grupo: 'Académico', roles: ADMIN, icono: 'cursos' },
  { ruta: '/datos/materias', etiqueta: 'Materias', grupo: 'Académico', roles: ADMIN, icono: 'materias' },
  { ruta: '/datos/asignaciones', etiqueta: 'Asignaciones', grupo: 'Académico', roles: ADMIN, icono: 'asignaciones' },
  { ruta: '/servicios', etiqueta: 'Inscripciones', grupo: 'Servicios', roles: GESTION, icono: 'inscripciones' },
  { ruta: '/datos/deportes', etiqueta: 'Deportes', grupo: 'Servicios', roles: ADMIN, icono: 'deportes' },
  { ruta: '/datos/instalaciones', etiqueta: 'Instalaciones', grupo: 'Servicios', roles: ADMIN, icono: 'instalaciones' },
  { ruta: '/datos/transporte', etiqueta: 'Transporte', grupo: 'Servicios', roles: ADMIN, icono: 'transporte' },
  { ruta: '/reportes', etiqueta: 'Reportes', grupo: '', roles: GESTION, icono: 'reportes' },
  { ruta: '/usuarios', etiqueta: 'Usuarios', grupo: 'Administración', roles: ADMIN, icono: 'usuarios' },
  { ruta: '/permisos', etiqueta: 'Permisos', grupo: 'Administración', roles: ADMIN, icono: 'permisos' },
  { ruta: '/mi-informacion', etiqueta: 'Mi información', grupo: '', roles: ['PADRE', 'ALUMNO'], icono: 'familia' },
  { ruta: '/mis-calificaciones', etiqueta: 'Calificaciones', grupo: '', roles: ['PADRE', 'ALUMNO'], icono: 'calificaciones' },
  { ruta: '/mis-servicios', etiqueta: 'Mis servicios', grupo: '', roles: ['PADRE', 'ALUMNO'], icono: 'misServicios' },
];

export function itemsPermitidos(rol: string | null): ItemMenu[] {
  if (!rol) return [];
  return ITEMS_MENU.filter((item) => item.roles.includes(rol));
}

export function puedeVer(rol: string | null, ruta: string): boolean {
  return itemsPermitidos(rol).some((item) => item.ruta === ruta);
}

/** Agrupa los accesos respetando el orden en que aparecen; el grupo vacío va primero. */
export function agruparMenu(items: ItemMenu[]): { titulo: string; items: ItemMenu[] }[] {
  const grupos: { titulo: string; items: ItemMenu[] }[] = [];
  for (const item of items) {
    let grupo = grupos.find((g) => g.titulo === item.grupo);
    if (!grupo) {
      grupo = { titulo: item.grupo, items: [] };
      grupos.push(grupo);
    }
    grupo.items.push(item);
  }
  return grupos;
}

export function rutaInicial(rol: string | null): string {
  if (rol === 'PADRE' || rol === 'ALUMNO') return '/mi-informacion';
  if (rol === 'PROFESOR') return '/alumnos';
  return '/inicio';
}

export function esSeccionDatos(valor: string | undefined): valor is SeccionDatos {
  return SECCIONES_DATOS.some((seccion) => seccion === valor);
}
