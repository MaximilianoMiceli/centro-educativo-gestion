import { useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import React from 'react';
import { ScrollView, Text } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../auth/AuthContext';
import { Boton, Tarjeta, estilos } from '../componentes/ui';
import type { RaizParams } from '../navegacion/tipos';

export function PerfilPantalla() {
  const { sesion, cerrar } = useAuth();
  const navegacion = useNavigation<NativeStackNavigationProp<RaizParams>>();

  return (
    <SafeAreaView style={estilos.pantalla} edges={['left', 'right']}>
      <ScrollView contentContainerStyle={estilos.contenido}>
        <Tarjeta>
          <Text style={estilos.textoGris}>Usuario</Text>
          <Text style={estilos.subtitulo}>{sesion?.username}</Text>
          <Text style={estilos.textoGris}>Perfil</Text>
          <Text style={estilos.subtitulo}>{sesion?.rol === 'PADRE' ? 'Padre o madre' : 'Alumno'}</Text>
        </Tarjeta>
        <Boton titulo="Cambiar contraseña" variante="secundario" onPress={() => navegacion.navigate('CambiarPassword')} />
        <Boton titulo="Cerrar sesión" variante="peligro" onPress={() => void cerrar()} />
      </ScrollView>
    </SafeAreaView>
  );
}
