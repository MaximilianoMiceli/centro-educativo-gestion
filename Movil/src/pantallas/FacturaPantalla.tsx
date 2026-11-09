import { Ionicons } from '@expo/vector-icons';
import type { NativeStackScreenProps } from '@react-navigation/native-stack';
import * as DocumentPicker from 'expo-document-picker';
import React, { useState } from 'react';
import { Alert, Pressable, RefreshControl, ScrollView, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { factura as pedirFactura, registrarPago } from '../api/backend';
import { ApiError } from '../api/cliente';
import type { ArchivoAdjunto } from '../api/tipos';
import { useAuth } from '../auth/AuthContext';
import { Aviso, Boton, CampoTexto, Cargando, Insignia, Tarjeta, estilos } from '../componentes/ui';
import type { RaizParams } from '../navegacion/tipos';
import { colores, espacio } from '../tema';
import { alternar, esFechaValida, hoyISO, itemsPendientes, totalElegido } from '../utils/facturas';
import { etiquetaConcepto, fecha, moneda, periodoLegible } from '../utils/formato';
import { descargarYAbrir } from '../utils/pdf';
import { useCarga } from '../utils/useCarga';

type Props = NativeStackScreenProps<RaizParams, 'Factura'>;

export function FacturaPantalla({ route }: Props) {
  const { facturaId } = route.params;
  const { sesion } = useAuth();
  const esPadre = sesion?.rol === 'PADRE';
  const { datos: factura, cargando, error, recargar } = useCarga(() => pedirFactura(facturaId), [facturaId]);

  const [elegidos, setElegidos] = useState<ReadonlySet<number>>(new Set());
  const [fechaPago, setFechaPago] = useState(hoyISO());
  const [archivo, setArchivo] = useState<ArchivoAdjunto | null>(null);
  const [enviando, setEnviando] = useState(false);
  const [errorPago, setErrorPago] = useState<string | null>(null);

  if (!factura) {
    return error ? <Aviso texto={error} /> : <Cargando />;
  }

  const pendientes = itemsPendientes(factura);
  const total = totalElegido(factura, elegidos);
  const fechaOk = esFechaValida(fechaPago);

  async function abrir(ruta: string, nombre: string) {
    try {
      await descargarYAbrir(ruta, nombre);
    } catch (e) {
      Alert.alert('No se pudo abrir', e instanceof ApiError ? e.message : 'Intentá de nuevo en unos minutos.');
    }
  }

  async function elegirArchivo() {
    const resultado = await DocumentPicker.getDocumentAsync({
      type: ['application/pdf', 'image/*'],
      copyToCacheDirectory: true,
    });
    if (!resultado.canceled && resultado.assets[0]) {
      const a = resultado.assets[0];
      setArchivo({ uri: a.uri, name: a.name, type: a.mimeType ?? 'application/octet-stream' });
    }
  }

  async function informarPago() {
    if (!archivo || !factura) {
      return;
    }
    setErrorPago(null);
    setEnviando(true);
    try {
      const pago = await registrarPago(factura.id, [...elegidos], fechaPago, archivo);
      setElegidos(new Set());
      setArchivo(null);
      Alert.alert('Pago registrado', `Se emitió el comprobante ${pago.numeroComprobante}.`);
      recargar();
    } catch (e) {
      setErrorPago(e instanceof ApiError ? e.textoCompleto() : 'No se pudo registrar el pago');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={estilos.pantalla} edges={['left', 'right', 'bottom']}>
      <ScrollView
        contentContainerStyle={estilos.contenido}
        refreshControl={<RefreshControl refreshing={cargando} onRefresh={recargar} />}
      >
        <Tarjeta>
          <View style={estilos.fila}>
            <Text style={estilos.titulo}>{periodoLegible(factura.periodo)}</Text>
            <Insignia estado={factura.vencida ? 'VENCIDO' : factura.estado} />
          </View>
          <Text style={estilos.textoGris}>{factura.numero} · {factura.alumno}</Text>
          <Text style={estilos.textoGris}>Emitida el {fecha(factura.emision)} · Vence el {fecha(factura.vencimiento)}</Text>
          <View style={{ marginTop: espacio.m }}>
            <Fila etiqueta="Total" valor={moneda(factura.total)} />
            <Fila etiqueta="Pagado" valor={moneda(factura.pagado)} />
            <Fila etiqueta="Saldo" valor={moneda(factura.saldo)} fuerte />
          </View>
          <Boton
            titulo="Ver factura en PDF"
            variante="secundario"
            onPress={() => void abrir(`/mi-informacion/facturas/${factura.id}/pdf`, `${factura.numero}.pdf`)}
          />
        </Tarjeta>

        <Text style={estilos.subtitulo}>Detalle</Text>
        <Tarjeta>
          {factura.items.map((item) => {
            const elegible = esPadre && !item.pagado;
            const marcado = elegidos.has(item.id);
            return (
              <Pressable
                key={item.id}
                accessibilityRole={elegible ? 'checkbox' : undefined}
                accessibilityState={elegible ? { checked: marcado } : undefined}
                disabled={!elegible}
                onPress={() => setElegidos(alternar(elegidos, item.id))}
                style={[estilos.fila, { paddingVertical: espacio.s }]}
              >
                <View style={{ flexDirection: 'row', alignItems: 'center', flex: 1, gap: espacio.s }}>
                  {elegible ? (
                    <Ionicons
                      name={marcado ? 'checkbox' : 'square-outline'}
                      size={24}
                      color={marcado ? colores.azul : colores.gris}
                    />
                  ) : (
                    <Ionicons name={item.pagado ? 'checkmark-circle' : 'ellipse-outline'} size={24} color={item.pagado ? colores.verdeOscuro : colores.gris} />
                  )}
                  <View style={{ flex: 1 }}>
                    <Text style={estilos.texto}>{item.descripcion}</Text>
                    <Text style={estilos.textoGris}>{etiquetaConcepto(item.concepto)}{item.pagado ? ' · Pagado' : ''}</Text>
                  </View>
                </View>
                <Text style={estilos.texto}>{moneda(item.monto)}</Text>
              </Pressable>
            );
          })}
        </Tarjeta>

        {factura.pagos.length > 0 ? (
          <View>
            <Text style={estilos.subtitulo}>Pagos y comprobantes</Text>
            {factura.pagos.map((pago) => (
              <Tarjeta key={pago.id}>
                <View style={estilos.fila}>
                  <Text style={estilos.subtitulo}>{pago.numeroComprobante}</Text>
                  <Text style={estilos.texto}>{moneda(pago.monto)}</Text>
                </View>
                <Text style={estilos.textoGris}>Transferencia del {fecha(pago.fechaPago)} · {pago.nombreArchivo}</Text>
                <Boton
                  titulo="Ver comprobante"
                  variante="secundario"
                  onPress={() => void abrir(`/mi-informacion/pagos/${pago.id}/comprobante`, `${pago.numeroComprobante}.pdf`)}
                />
              </Tarjeta>
            ))}
          </View>
        ) : null}

        {esPadre && pendientes.length > 0 ? (
          <View>
            <Text style={estilos.subtitulo}>Informar un pago</Text>
            <Tarjeta>
              <Text style={[estilos.textoGris, { marginBottom: espacio.m }]}>
                Los pagos se hacen por transferencia bancaria. Elegí los ítems que pagaste, indicá la fecha y adjuntá el
                comprobante de la transferencia.
              </Text>
              {errorPago ? <Aviso texto={errorPago} /> : null}
              <Text style={[estilos.texto, { marginBottom: espacio.s }]}>
                Ítems elegidos: {elegidos.size} · Total {moneda(total)}
              </Text>
              <CampoTexto
                etiqueta="Fecha de la transferencia"
                placeholder="AAAA-MM-DD"
                value={fechaPago}
                onChangeText={setFechaPago}
                error={fechaOk ? undefined : 'Usá el formato AAAA-MM-DD'}
              />
              <Boton
                titulo={archivo ? `Archivo: ${archivo.name}` : 'Adjuntar comprobante (PDF o imagen)'}
                variante="secundario"
                onPress={() => void elegirArchivo()}
              />
              <Boton
                titulo={`Informar pago de ${moneda(total)}`}
                onPress={() => void informarPago()}
                cargando={enviando}
                deshabilitado={elegidos.size === 0 || !archivo || !fechaOk}
              />
            </Tarjeta>
          </View>
        ) : null}
        {!esPadre ? (
          <Aviso tipo="info" texto="Los pagos los informan los padres desde su cuenta." />
        ) : null}
      </ScrollView>
    </SafeAreaView>
  );
}

function Fila({ etiqueta, valor, fuerte }: { etiqueta: string; valor: string; fuerte?: boolean }) {
  return (
    <View style={[estilos.fila, { paddingVertical: 2 }]}>
      <Text style={estilos.texto}>{etiqueta}</Text>
      <Text style={[estilos.texto, fuerte && { fontWeight: '700', color: colores.azul }]}>{valor}</Text>
    </View>
  );
}
