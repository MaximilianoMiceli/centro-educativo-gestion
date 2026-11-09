import { useNavigation } from '@react-navigation/native';
import React, { useState } from 'react';
import { Alert, KeyboardAvoidingView, Platform, ScrollView, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { cambiarPassword } from '../api/backend';
import { ApiError } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import { Aviso, Boton, CampoTexto, estilos } from '../componentes/ui';
import { espacio } from '../tema';

const MINIMO = 8;
const MAXIMO = 72;

export function CambiarPasswordPantalla() {
  const { sesion, actualizar, cerrar } = useAuth();
  const navegacion = useNavigation();
  const obligatorio = sesion?.debeCambiarPassword === true;
  const [actual, setActual] = useState('');
  const [nueva, setNueva] = useState('');
  const [repetida, setRepetida] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  const errorNueva =
    nueva.length > 0 && (nueva.length < MINIMO || nueva.length > MAXIMO)
      ? `Debe tener entre ${MINIMO} y ${MAXIMO} caracteres`
      : undefined;
  const errorRepetida = repetida.length > 0 && repetida !== nueva ? 'Las contraseñas no coinciden' : undefined;
  const valido = actual.length > 0 && nueva.length >= MINIMO && nueva.length <= MAXIMO && nueva === repetida;

  async function guardar() {
    setError(null);
    setEnviando(true);
    try {
      const nuevaSesion = await cambiarPassword(actual, nueva);
      await actualizar({
        token: nuevaSesion.token,
        username: nuevaSesion.username,
        rol: nuevaSesion.rol,
        debeCambiarPassword: false,
      });
      Alert.alert('Contraseña actualizada', 'Ya podés usarla para ingresar.');
      if (!obligatorio && navegacion.canGoBack()) {
        navegacion.goBack();
      }
    } catch (e) {
      setError(e instanceof ApiError ? e.textoCompleto() : 'No se pudo cambiar la contraseña');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={estilos.pantalla} edges={obligatorio ? ['top', 'bottom'] : ['bottom']}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={estilos.contenido}>
          {obligatorio ? (
            <Aviso tipo="info" texto="Es tu primer ingreso: elegí una contraseña nueva para continuar." />
          ) : null}
          {error ? <Aviso texto={error} /> : null}
          <CampoTexto etiqueta="Contraseña actual" value={actual} onChangeText={setActual} secureTextEntry autoCapitalize="none" />
          <CampoTexto
            etiqueta="Contraseña nueva"
            value={nueva}
            onChangeText={setNueva}
            secureTextEntry
            autoCapitalize="none"
            error={errorNueva}
          />
          <CampoTexto
            etiqueta="Repetí la contraseña nueva"
            value={repetida}
            onChangeText={setRepetida}
            secureTextEntry
            autoCapitalize="none"
            error={errorRepetida}
          />
          <Boton titulo="Guardar contraseña" onPress={guardar} cargando={enviando} deshabilitado={!valido} />
          {obligatorio ? (
            <Boton titulo="Cerrar sesión" variante="secundario" onPress={() => void cerrar()} />
          ) : null}
          <View style={{ height: espacio.xl }} />
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
