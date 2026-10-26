import React from 'react';

interface LogoInstitucionalProps {
  size?: number;
}

/** Logo oficial del centro (PNG con fondo transparente). */
export const LogoInstitucional: React.FC<LogoInstitucionalProps> = ({ size = 40 }) => (
  <img
    src="/logo-institucional.png"
    width={size}
    height={size}
    alt="Logo Educar Para Transformar"
    style={{ display: 'block', objectFit: 'contain' }}
  />
);
