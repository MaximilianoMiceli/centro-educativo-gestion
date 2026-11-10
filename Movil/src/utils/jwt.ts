const ALFABETO = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/';

/** Decodifica base64 o base64url a texto UTF-8, sin depender de atob. */
function base64ATexto(entrada: string): string {
  const limpio = entrada.replace(/-/g, '+').replace(/_/g, '/').replace(/=+$/, '');
  const bytes: number[] = [];
  let acumulado = 0;
  let bits = 0;
  for (const caracter of limpio) {
    const valor = ALFABETO.indexOf(caracter);
    if (valor < 0) {
      throw new Error('Token inválido');
    }
    acumulado = (acumulado << 6) | valor;
    bits += 6;
    if (bits >= 8) {
      bits -= 8;
      bytes.push((acumulado >> bits) & 0xff);
    }
  }
  return decodeURIComponent(bytes.map((b) => `%${b.toString(16).padStart(2, '0')}`).join(''));
}

export interface DatosToken {
  usuarioId: string;
  username: string;
  rol: string;
  debeCambiarPassword: boolean;
  /** Segundos desde 1970, si el token trae vencimiento. */
  vence?: number;
}

export function leerToken(token: string): DatosToken {
  const partes = token.split('.');
  if (partes.length !== 3) {
    throw new Error('Token inválido');
  }
  const datos = JSON.parse(base64ATexto(partes[1])) as Record<string, unknown>;
  return {
    usuarioId: String(datos.sub ?? ''),
    username: String(datos.username ?? ''),
    rol: String(datos.rol ?? ''),
    debeCambiarPassword: datos.debeCambiarPassword === true,
    vence: typeof datos.exp === 'number' ? datos.exp : undefined,
  };
}

export function estaVencido(datos: DatosToken, ahoraMs: number = Date.now()): boolean {
  return datos.vence !== undefined && datos.vence * 1000 <= ahoraMs;
}
