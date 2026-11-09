import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import React, { useState } from 'react';
import { FlatList, Pressable, RefreshControl, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { facturas as pedirFacturas } from '../api/backend';
import type { Factura } from '../api/tipos';
import { Aviso, Cargando, Chips, CampoTexto, Insignia, SelectorHijo, Tarjeta, Vacio, estilos } from '../componentes/ui';
import { useFamilia } from '../familia/FamiliaContext';
import type { RaizParams } from '../navegacion/tipos';
import { colores, espacio } from '../tema';
import { esFechaValida, filtrarPorEstado, type FiltroEstado } from '../utils/facturas';
import { fecha, moneda, periodoLegible } from '../utils/formato';
import { useCarga } from '../utils/useCarga';

const FILTROS: { valor: FiltroEstado; etiqueta: string }[] = [
  { valor: 'PENDIENTES', etiqueta: 'Pendientes' },
  { valor: 'PAGADAS', etiqueta: 'Pagadas' },
  { valor: 'TODAS', etiqueta: 'Todas' },
];

export function CuotasPantalla() {
  const navegacion = useNavigation<NativeStackNavigationProp<RaizParams>>();
  const { hijo, cargando: cargandoHijos, error: errorHijos } = useFamilia();
  const [filtro, setFiltro] = useState<FiltroEstado>('PENDIENTES');
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [rango, setRango] = useState<{ desde?: string; hasta?: string }>({});

  const errorFecha =
    (desde && !esFechaValida(desde)) || (hasta && !esFechaValida(hasta)) ? 'Usá el formato AAAA-MM-DD' : undefined;
  const { datos, cargando, error, recargar } = useCarga<Factura[]>(
    hijo ? () => pedirFacturas(hijo.id, rango) : null,
    [hijo?.id, rango.desde, rango.hasta],
  );

  if (cargandoHijos) {
    return <Cargando />;
  }
  if (errorHijos) {
    return <Aviso texto={errorHijos} />;
  }
  if (!hijo) {
    return <Vacio texto="Tu cuenta no tiene alumnos vinculados. Consultá con la administración." />;
  }

  const visibles = filtrarPorEstado(datos ?? [], filtro);
  return (
    <SafeAreaView style={estilos.pantalla} edges={['left', 'right']}>
      <FlatList
        data={visibles}
        keyExtractor={(f) => String(f.id)}
        contentContainerStyle={estilos.contenido}
        refreshControl={<RefreshControl refreshing={cargando} onRefresh={recargar} />}
        ListHeaderComponent={
          <View>
            <SelectorHijo />
            <Chips opciones={FILTROS} valor={filtro} onCambio={setFiltro} />
            <View style={{ flexDirection: 'row', gap: espacio.s }}>
              <View style={{ flex: 1 }}>
                <CampoTexto etiqueta="Desde" placeholder="AAAA-MM-DD" value={desde} onChangeText={setDesde} />
              </View>
              <View style={{ flex: 1 }}>
                <CampoTexto etiqueta="Hasta" placeholder="AAAA-MM-DD" value={hasta} onChangeText={setHasta} />
              </View>
            </View>
            {errorFecha ? <Aviso texto={errorFecha} /> : null}
            <Pressable
              accessibilityRole="button"
              disabled={!!errorFecha}
              onPress={() => setRango({ desde: desde || undefined, hasta: hasta || undefined })}
              style={{ alignSelf: 'flex-end', marginBottom: espacio.m }}
            >
              <Text style={{ color: errorFecha ? colores.gris : colores.azul, fontWeight: '700' }}>Filtrar por fechas</Text>
            </Pressable>
            {error ? <Aviso texto={error} /> : null}
          </View>
        }
        ListEmptyComponent={
          cargando ? <Cargando /> : <Vacio texto="No hay cuotas para mostrar con este filtro." />
        }
        renderItem={({ item }) => (
          <Pressable accessibilityRole="button" onPress={() => navegacion.navigate('Factura', { facturaId: item.id })}>
            <Tarjeta>
              <View style={estilos.fila}>
                <Text style={estilos.subtitulo}>{periodoLegible(item.periodo)}</Text>
                <Insignia estado={item.vencida ? 'VENCIDO' : item.estado} />
              </View>
              <Text style={estilos.textoGris}>{item.numero}</Text>
              <View style={[estilos.fila, { marginTop: espacio.s }]}>
                <Text style={estilos.texto}>Total {moneda(item.total)}</Text>
                <Text style={[estilos.texto, { fontWeight: '700' }]}>Saldo {moneda(item.saldo)}</Text>
              </View>
              <Text style={estilos.textoGris}>Vence el {fecha(item.vencimiento)}</Text>
            </Tarjeta>
          </Pressable>
        )}
      />
    </SafeAreaView>
  );
}
