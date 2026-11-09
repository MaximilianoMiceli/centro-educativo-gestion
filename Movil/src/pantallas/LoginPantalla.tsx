import React, { useState } from 'react';
import { Image, KeyboardAvoidingView, Platform, ScrollView, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { ApiError } from '../api/cliente';
import { useAuth } from '../auth/AuthContext';
import { Aviso, Boton, CampoTexto, estilos } from '../componentes/ui';
import { colores, espacio } from '../tema';

export function LoginPantalla() {
  const { iniciar } = useAuth();
  const [usuario, setUsuario] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function ingresar() {
    setError(null);
    setEnviando(true);
    try {
      await iniciar(usuario, password);
    } catch (e) {
      setError(e instanceof ApiError ? e.message : 'No se pudo iniciar sesión');
      setEnviando(false);
    }
  }

  return (
    <SafeAreaView style={estilos.pantalla}>
      <KeyboardAvoidingView behavior={Platform.OS === 'ios' ? 'padding' : undefined} style={{ flex: 1 }}>
        <ScrollView contentContainerStyle={{ padding: espacio.xl, flexGrow: 1, justifyContent: 'center' }}>
          <View style={{ alignItems: 'center', marginBottom: espacio.xl }}>
            <Image source={require('../../assets/logo.png')} style={{ width: 110, height: 110 }} resizeMode="contain" />
            <Text style={[estilos.titulo, { marginTop: espacio.m, textAlign: 'center' }]}>Educar Para Transformar</Text>
            <Text style={estilos.textoGris}>Cuotas y servicios para las familias</Text>
          </View>
          {error ? <Aviso texto={error} /> : null}
          <CampoTexto
            etiqueta="Usuario"
            value={usuario}
            onChangeText={setUsuario}
            autoCapitalize="none"
            autoCorrect={false}
            textContentType="username"
          />
          <CampoTexto
            etiqueta="Contraseña"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
            autoCapitalize="none"
            textContentType="password"
            onSubmitEditing={ingresar}
          />
          <Boton titulo="Ingresar" onPress={ingresar} cargando={enviando} deshabilitado={!usuario || !password} />
          <Text style={{ color: colores.gris, textAlign: 'center', marginTop: espacio.l, fontSize: 12 }}>
            Si olvidaste tu contraseña, pedí que la restablezcan en la administración del colegio.
          </Text>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}
