import React from 'react';
import { RefreshControl, ScrollView, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { miInformacion } from '../api/backend';
import { Aviso, Cargando, SelectorHijo, Tarjeta, Vacio, estilos, PantallaCompleta } from '../componentes/ui';
import { useFamilia } from '../familia/FamiliaContext';
import { colores } from '../tema';
import { useCarga } from '../utils/useCarga';

/** Inscripciones del alumno a deportes, transporte y comedor, en modo consulta. */
export function ServiciosPantalla() {
  const { hijo, cargando: cargandoHijos, error: errorHijos } = useFamilia();
  const { datos, cargando, error, recargar } = useCarga(hijo ? () => miInformacion(hijo.id) : null, [hijo?.id]);

  if (cargandoHijos) {
    return (
      <PantallaCompleta>
        <Cargando />
      </PantallaCompleta>
    );
  }
  if (errorHijos) {
    return (
      <PantallaCompleta>
        <Aviso texto={errorHijos} />
      </PantallaCompleta>
    );
  }
  if (!hijo) {
    return (
      <PantallaCompleta>
        <Vacio texto="Tu cuenta no tiene alumnos vinculados. Consultá con la administración." />
      </PantallaCompleta>
    );
  }
  const servicios = datos?.servicios;

  return (
    <SafeAreaView style={estilos.pantalla} edges={['left', 'right']}>
      <ScrollView
        contentContainerStyle={estilos.contenido}
        refreshControl={<RefreshControl refreshing={cargando} onRefresh={recargar} />}
      >
        <SelectorHijo />
        {error ? <Aviso texto={error} /> : null}
        {servicios ? (
          <View>
            <Text style={estilos.subtitulo}>Deportes</Text>
            <Tarjeta>
              {servicios.deportes.length === 0 ? (
                <Text style={estilos.textoGris}>No está inscripto en ningún deporte.</Text>
              ) : (
                servicios.deportes.map((d) => (
                  <View key={d.id} style={{ paddingVertical: 4 }}>
                    <Text style={estilos.texto}>{d.deporte}</Text>
                    <Text style={estilos.textoGris}>{d.horario}</Text>
                  </View>
                ))
              )}
            </Tarjeta>

            <Text style={estilos.subtitulo}>Transporte</Text>
            <Tarjeta>
              <Text style={servicios.transporte ? estilos.texto : estilos.textoGris}>
                {servicios.transporte ? servicios.transporte.nombre : 'No usa el servicio de transporte.'}
              </Text>
            </Tarjeta>

            <Text style={estilos.subtitulo}>Comedor</Text>
            <Tarjeta>
              <Text style={servicios.comedor ? estilos.texto : estilos.textoGris}>
                {servicios.comedor ? 'Inscripto en el comedor.' : 'No usa el servicio de comedor.'}
              </Text>
            </Tarjeta>
            <Text style={{ color: colores.gris, fontSize: 12 }}>
              Las inscripciones se modifican desde el sistema del colegio. El valor de cada servicio se suma a la factura
              del mes siguiente.
            </Text>
          </View>
        ) : cargando ? (
          <Cargando />
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}
