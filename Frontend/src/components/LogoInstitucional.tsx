import React from 'react';

interface LogoInstitucionalProps {
  size?: number;
}

/** Logo del centro, mismo diseño que el sitio institucional y que el encabezado de los reportes. */
export const LogoInstitucional: React.FC<LogoInstitucionalProps> = ({ size = 40 }) => (
  <svg width={size} height={size} viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Logo Educar para Transformar">
    <rect x="6" y="19" width="16" height="20" rx="3" fill="#1a5fa8" opacity=".2" />
    <rect x="26" y="19" width="16" height="20" rx="3" fill="#1a5fa8" opacity=".3" />
    <rect x="22" y="18" width="4" height="22" rx="2" fill="#1a5fa8" />
    <line x1="9" y1="25" x2="19" y2="25" stroke="#1a5fa8" strokeWidth="1.3" strokeLinecap="round" opacity=".5" />
    <line x1="9" y1="29" x2="19" y2="29" stroke="#1a5fa8" strokeWidth="1.3" strokeLinecap="round" opacity=".5" />
    <path d="M24 18 Q20 10 13 8 Q18 14 24 18Z" fill="#4caf82" />
    <path d="M24 18 Q28 9 35 7 Q30 13 24 18Z" fill="#4caf82" opacity=".85" />
    <line x1="24" y1="18" x2="24" y2="7" stroke="#4caf82" strokeWidth="1.5" strokeLinecap="round" />
    <circle cx="24" cy="6" r="2.2" fill="#f4894a" />
    <circle cx="15" cy="14" r="2" fill="#1a5fa8" opacity=".6" />
    <circle cx="33" cy="13" r="2" fill="#1a5fa8" opacity=".6" />
  </svg>
);
