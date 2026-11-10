import { StatusBar } from 'expo-status-bar';
import React from 'react';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { AuthProvider } from './src/auth/AuthContext';
import { FamiliaProvider } from './src/familia/FamiliaContext';
import { Navegador } from './src/navegacion/Navegador';

export default function App() {
  return (
    <SafeAreaProvider>
      <AuthProvider>
        <FamiliaProvider>
          <Navegador />
          <StatusBar style="light" />
        </FamiliaProvider>
      </AuthProvider>
    </SafeAreaProvider>
  );
}
