import { Ionicons } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import React from 'react';
import { useAuth } from '../auth/AuthContext';
import { Cargando } from '../componentes/ui';
import { CambiarPasswordPantalla } from '../pantallas/CambiarPasswordPantalla';
import { CuotasPantalla } from '../pantallas/CuotasPantalla';
import { DeudaPantalla } from '../pantallas/DeudaPantalla';
import { FacturaPantalla } from '../pantallas/FacturaPantalla';
import { LoginPantalla } from '../pantallas/LoginPantalla';
import { PerfilPantalla } from '../pantallas/PerfilPantalla';
import { ServiciosPantalla } from '../pantallas/ServiciosPantalla';
import { colores } from '../tema';
import type { RaizParams } from './tipos';

const Pila = createNativeStackNavigator<RaizParams>();
const Pestanias = createBottomTabNavigator();

const ICONOS: Record<string, keyof typeof Ionicons.glyphMap> = {
  Cuotas: 'wallet-outline',
  Deuda: 'receipt-outline',
  Servicios: 'football-outline',
  Perfil: 'person-outline',
};

function Principal() {
  return (
    <Pestanias.Navigator
      screenOptions={({ route }) => ({
        headerStyle: { backgroundColor: colores.azul },
        headerTintColor: colores.blanco,
        tabBarActiveTintColor: colores.azul,
        tabBarIcon: ({ color, size }) => <Ionicons name={ICONOS[route.name]} size={size} color={color} />,
      })}
    >
      <Pestanias.Screen name="Cuotas" component={CuotasPantalla} />
      <Pestanias.Screen name="Deuda" component={DeudaPantalla} options={{ title: 'Historial de deuda' }} />
      <Pestanias.Screen name="Servicios" component={ServiciosPantalla} />
      <Pestanias.Screen name="Perfil" component={PerfilPantalla} />
    </Pestanias.Navigator>
  );
}

const opcionesCabecera = {
  headerStyle: { backgroundColor: colores.azul },
  headerTintColor: colores.blanco,
};

/** Sin sesión solo se ve el ingreso; con contraseña provisoria, solo el cambio; después, la app completa. */
export function Navegador() {
  const { sesion, cargando } = useAuth();
  if (cargando) {
    return <Cargando />;
  }
  return (
    <NavigationContainer>
      <Pila.Navigator screenOptions={opcionesCabecera}>
        {!sesion ? (
          <Pila.Screen name="Principal" component={LoginPantalla} options={{ headerShown: false }} />
        ) : sesion.debeCambiarPassword ? (
          <Pila.Screen
            name="CambiarPassword"
            component={CambiarPasswordPantalla}
            options={{ title: 'Elegí una contraseña nueva', headerBackVisible: false }}
          />
        ) : (
          <>
            <Pila.Screen name="Principal" component={Principal} options={{ headerShown: false }} />
            <Pila.Screen name="Factura" component={FacturaPantalla} options={{ title: 'Factura' }} />
            <Pila.Screen name="CambiarPassword" component={CambiarPasswordPantalla} options={{ title: 'Cambiar contraseña' }} />
          </>
        )}
      </Pila.Navigator>
    </NavigationContainer>
  );
}
