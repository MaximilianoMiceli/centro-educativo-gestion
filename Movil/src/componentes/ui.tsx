import React from 'react';
import {
  ActivityIndicator,
  Pressable,
  StyleSheet,
  Text,
  TextInput,
  type TextInputProps,
  View,
  type ViewStyle,
} from 'react-native';
import { useFamilia } from '../familia/FamiliaContext';
import { colores, espacio } from '../tema';
import { etiquetaEstado } from '../utils/formato';

export function Boton({
  titulo,
  onPress,
  deshabilitado,
  cargando,
  variante = 'primario',
}: {
  titulo: string;
  onPress: () => void;
  deshabilitado?: boolean;
  cargando?: boolean;
  variante?: 'primario' | 'secundario' | 'peligro';
}) {
  const inactivo = deshabilitado || cargando;
  return (
    <Pressable
      accessibilityRole="button"
      accessibilityState={{ disabled: !!inactivo }}
      onPress={onPress}
      disabled={inactivo}
      style={({ pressed }) => [
        estilos.boton,
        variante === 'secundario' && estilos.botonSecundario,
        variante === 'peligro' && estilos.botonPeligro,
        inactivo && estilos.botonInactivo,
        pressed && { opacity: 0.8 },
      ]}
    >
      {cargando ? (
        <ActivityIndicator color={variante === 'secundario' ? colores.azul : colores.blanco} />
      ) : (
        <Text style={[estilos.textoBoton, variante === 'secundario' && { color: colores.azul }]}>{titulo}</Text>
      )}
    </Pressable>
  );
}

export function CampoTexto({ etiqueta, error, ...resto }: TextInputProps & { etiqueta: string; error?: string }) {
  return (
    <View style={{ marginBottom: espacio.m }}>
      <Text style={estilos.etiqueta}>{etiqueta}</Text>
      <TextInput
        accessibilityLabel={etiqueta}
        placeholderTextColor={colores.gris}
        style={[estilos.campo, error ? { borderColor: colores.rojo } : null]}
        {...resto}
      />
      {error ? <Text style={estilos.textoError}>{error}</Text> : null}
    </View>
  );
}

export function Tarjeta({ children, estilo }: { children: React.ReactNode; estilo?: ViewStyle }) {
  return <View style={[estilos.tarjeta, estilo]}>{children}</View>;
}

const COLOR_ESTADO: Record<string, string> = {
  PENDIENTE: colores.naranja,
  PARCIAL: '#B7791F',
  PAGADA: colores.verdeOscuro,
  PAGADO: colores.verdeOscuro,
  VENCIDO: colores.rojo,
};

export function Insignia({ estado, texto }: { estado: string; texto?: string }) {
  const color = COLOR_ESTADO[estado] ?? colores.gris;
  return (
    <View style={[estilos.insignia, { borderColor: color }]}>
      <Text style={{ color, fontSize: 12, fontWeight: '700' }}>{texto ?? etiquetaEstado(estado)}</Text>
    </View>
  );
}

export function Aviso({ texto, tipo = 'error' }: { texto: string; tipo?: 'error' | 'info' }) {
  return (
    <View style={[estilos.aviso, tipo === 'info' && { backgroundColor: colores.azulSuave }]}>
      <Text style={{ color: tipo === 'error' ? colores.rojo : colores.azul }}>{texto}</Text>
    </View>
  );
}

/** Ocupa toda la pantalla con el fondo de la app, para los estados de carga, error o vacío. */
export function PantallaCompleta({ children }: { children: React.ReactNode }) {
  return <View style={[estilos.pantalla, { justifyContent: 'center' }]}>{children}</View>;
}

export function Cargando() {
  return (
    <View style={estilos.centro}>
      <ActivityIndicator size="large" color={colores.azul} />
    </View>
  );
}

export function Vacio({ texto }: { texto: string }) {
  return (
    <View style={estilos.centro}>
      <Text style={{ color: colores.gris, textAlign: 'center' }}>{texto}</Text>
    </View>
  );
}

export function Chips<T extends string>({
  opciones,
  valor,
  onCambio,
}: {
  opciones: { valor: T; etiqueta: string }[];
  valor: T;
  onCambio: (valor: T) => void;
}) {
  return (
    <View style={{ flexDirection: 'row', gap: espacio.s, marginBottom: espacio.m }}>
      {opciones.map((o) => (
        <Pressable
          key={o.valor}
          accessibilityRole="button"
          accessibilityState={{ selected: o.valor === valor }}
          onPress={() => onCambio(o.valor)}
          style={[estilos.chip, o.valor === valor && estilos.chipActivo]}
        >
          <Text style={{ color: o.valor === valor ? colores.blanco : colores.azul, fontWeight: '600' }}>
            {o.etiqueta}
          </Text>
        </Pressable>
      ))}
    </View>
  );
}

/** Si la cuenta tiene más de un hijo, permite elegir de cuál se ve la información. */
export function SelectorHijo() {
  const { hijos, hijo, elegir } = useFamilia();
  if (hijos.length < 2) {
    return hijo ? <Text style={estilos.nombreHijo}>{hijo.alumno} · {hijo.curso}</Text> : null;
  }
  return (
    <View style={{ flexDirection: 'row', flexWrap: 'wrap', gap: espacio.s, marginBottom: espacio.m }}>
      {hijos.map((h) => (
        <Pressable
          key={h.id}
          accessibilityRole="button"
          accessibilityState={{ selected: h.id === hijo?.id }}
          onPress={() => elegir(h.id)}
          style={[estilos.chip, h.id === hijo?.id && estilos.chipActivo]}
        >
          <Text style={{ color: h.id === hijo?.id ? colores.blanco : colores.azul, fontWeight: '600' }}>
            {h.alumno}
          </Text>
        </Pressable>
      ))}
    </View>
  );
}

export const estilos = StyleSheet.create({
  pantalla: { flex: 1, backgroundColor: colores.fondo },
  contenido: { padding: espacio.l },
  titulo: { fontSize: 22, fontWeight: '700', color: colores.azul, marginBottom: espacio.s },
  subtitulo: { fontSize: 16, fontWeight: '700', color: colores.texto, marginBottom: espacio.s },
  texto: { color: colores.texto, fontSize: 15 },
  textoGris: { color: colores.gris, fontSize: 13 },
  nombreHijo: { color: colores.texto, fontWeight: '700', marginBottom: espacio.m },
  fila: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  boton: {
    backgroundColor: colores.azul,
    borderRadius: 10,
    paddingVertical: 13,
    paddingHorizontal: espacio.l,
    alignItems: 'center',
    marginTop: espacio.s,
  },
  botonSecundario: { backgroundColor: colores.blanco, borderWidth: 1, borderColor: colores.azul },
  botonPeligro: { backgroundColor: colores.rojo },
  botonInactivo: { opacity: 0.45 },
  textoBoton: { color: colores.blanco, fontWeight: '700', fontSize: 15 },
  etiqueta: { color: colores.texto, fontWeight: '600', marginBottom: espacio.xs },
  campo: {
    backgroundColor: colores.blanco,
    borderWidth: 1,
    borderColor: colores.borde,
    borderRadius: 10,
    paddingHorizontal: espacio.m,
    paddingVertical: 11,
    fontSize: 16,
    color: colores.texto,
  },
  textoError: { color: colores.rojo, marginTop: espacio.xs, fontSize: 13 },
  tarjeta: {
    backgroundColor: colores.blanco,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colores.borde,
    padding: espacio.l,
    marginBottom: espacio.m,
  },
  insignia: { borderWidth: 1.5, borderRadius: 999, paddingHorizontal: 10, paddingVertical: 3 },
  aviso: { backgroundColor: '#FDECEC', borderRadius: 10, padding: espacio.m, marginBottom: espacio.m },
  centro: { padding: espacio.xl, alignItems: 'center', justifyContent: 'center' },
  chip: {
    borderWidth: 1.5,
    borderColor: colores.azul,
    borderRadius: 999,
    paddingHorizontal: espacio.m,
    paddingVertical: 6,
  },
  chipActivo: { backgroundColor: colores.azul },
});
