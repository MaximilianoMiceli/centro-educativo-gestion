import React, { useMemo, useState } from 'react';
import { Pressable, RefreshControl, SectionList, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { deuda as pedirDeuda } from '../api/backend';
import type { Deuda, ItemDeuda } from '../api/tipos';
import { Aviso, CampoTexto, Cargando, Insignia, SelectorHijo, Tarjeta, Vacio, estilos } from '../componentes/ui';
import { useFamilia } from '../familia/FamiliaContext';
import { colores, espacio } from '../tema';
import { esFechaValida } from '../utils/facturas';
import { etiquetaConcepto, fecha, moneda, periodoLegible } from '../utils/formato';
import { useCarga } from '../utils/useCarga';

interface Grupo {
  titulo: string;
  vencimiento: string;
  data: ItemDeuda[];
}

/** Agrupa los ítems por factura, conservando el orden en que llegan del servidor. */
export function agruparPorFactura(items: ItemDeuda[]): Grupo[] {
  const grupos = new Map<number, Grupo>();
  for (const item of items) {
    const grupo = grupos.get(item.facturaId) ?? {
      titulo: `${periodoLegible(item.periodo)} · ${item.numeroFactura}`,
      vencimiento: item.vencimiento,
      data: [],
    };
    grupo.data.push(item);
    grupos.set(item.facturaId, grupo);
  }
  return [...grupos.values()];
}

export function DeudaPantalla() {
  const { hijo, cargando: cargandoHijos, error: errorHijos } = useFamilia();
  const [desde, setDesde] = useState('');
  const [hasta, setHasta] = useState('');
  const [rango, setRango] = useState<{ desde?: string; hasta?: string }>({});
  const errorFecha =
    (desde && !esFechaValida(desde)) || (hasta && !esFechaValida(hasta)) ? 'Usá el formato AAAA-MM-DD' : undefined;

  const { datos, cargando, error, recargar } = useCarga<Deuda>(
    hijo ? () => pedirDeuda(hijo.id, rango) : null,
    [hijo?.id, rango.desde, rango.hasta],
  );
  const secciones = useMemo(() => agruparPorFactura(datos?.items ?? []), [datos]);

  if (cargandoHijos) {
    return <Cargando />;
  }
  if (errorHijos) {
    return <Aviso texto={errorHijos} />;
  }
  if (!hijo) {
    return <Vacio texto="Tu cuenta no tiene alumnos vinculados. Consultá con la administración." />;
  }

  return (
    <SafeAreaView style={estilos.pantalla} edges={['left', 'right']}>
      <SectionList
        sections={secciones}
        keyExtractor={(item, indice) => `${item.facturaId}-${indice}`}
        contentContainerStyle={estilos.contenido}
        refreshControl={<RefreshControl refreshing={cargando} onRefresh={recargar} />}
        ListHeaderComponent={
          <View>
            <SelectorHijo />
            {datos ? (
              <Tarjeta>
                <Resumen etiqueta="Facturado" valor={moneda(datos.totalFacturado)} />
                <Resumen etiqueta="Pagado" valor={moneda(datos.totalPagado)} />
                <Resumen etiqueta="Adeudado" valor={moneda(datos.totalAdeudado)} fuerte />
              </Tarjeta>
            ) : null}
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
        ListEmptyComponent={cargando ? <Cargando /> : <Vacio texto="Todavía no hay facturas en este período." />}
        renderSectionHeader={({ section }) => (
          <View style={{ backgroundColor: colores.fondo, paddingVertical: espacio.s }}>
            <Text style={estilos.subtitulo}>{section.titulo}</Text>
            <Text style={estilos.textoGris}>Vence el {fecha(section.vencimiento)}</Text>
          </View>
        )}
        renderItem={({ item }) => (
          <Tarjeta estilo={{ marginBottom: espacio.s, paddingVertical: espacio.m }}>
            <View style={estilos.fila}>
              <View style={{ flex: 1, paddingRight: espacio.s }}>
                <Text style={estilos.texto}>{item.descripcion}</Text>
                <Text style={estilos.textoGris}>{etiquetaConcepto(item.concepto)}</Text>
              </View>
              <View style={{ alignItems: 'flex-end', gap: espacio.xs }}>
                <Text style={[estilos.texto, { fontWeight: '700' }]}>{moneda(item.monto)}</Text>
                <Insignia estado={item.estado} />
              </View>
            </View>
          </Tarjeta>
        )}
      />
    </SafeAreaView>
  );
}

function Resumen({ etiqueta, valor, fuerte }: { etiqueta: string; valor: string; fuerte?: boolean }) {
  return (
    <View style={[estilos.fila, { paddingVertical: 2 }]}>
      <Text style={estilos.texto}>{etiqueta}</Text>
      <Text style={[estilos.texto, fuerte && { fontWeight: '700', color: colores.azul }]}>{valor}</Text>
    </View>
  );
}
