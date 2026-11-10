// Inicia Expo para probar la app desde un celular con Expo Go.
// Detecta la dirección de esta PC en la red local y la usa como servidor de la API,
// así no hay que escribirla a mano (cambia cuando se cambia de red).
// Uso: npm run celular            (opcional: PUERTO_API=8080)
//      npm run celular -- --dry   (solo muestra la dirección detectada)
import { spawn } from 'node:child_process';
import { networkInterfaces } from 'node:os';

const puertoApi = process.env.PUERTO_API ?? '8080';

/** Una dirección de red local típica (192.168.x.x, 10.x.x.x, 172.16-31.x.x). Se descartan VPN como Tailscale (100.x). */
function esRedLocal(ip) {
  if (ip.startsWith('192.168.') || ip.startsWith('10.')) return true;
  const m = /^172\.(\d+)\./.exec(ip);
  return m !== null && Number(m[1]) >= 16 && Number(m[1]) <= 31;
}

function direccionLocal() {
  const candidatas = Object.values(networkInterfaces())
    .flat()
    .filter((i) => i && i.family === 'IPv4' && !i.internal)
    .map((i) => i.address)
    .filter(esRedLocal);
  return candidatas[0];
}

const ip = direccionLocal();
if (!ip) {
  console.error('No se encontró una dirección de red local. Conectá la PC a la misma red wifi que el celular.');
  process.exit(1);
}

const api = `http://${ip}:${puertoApi}/api/v1`;
console.log(`\nLa app del celular va a usar el servidor: ${api}`);
console.log('El celular y la PC tienen que estar en la misma red wifi.\n');

if (process.argv.includes('--dry')) process.exit(0);

const proceso = spawn('npx', ['expo', 'start', '--go', '--lan', '--offline'], {
  stdio: 'inherit',
  shell: true,
  env: { ...process.env, EXPO_PUBLIC_API_URL: api },
});
proceso.on('exit', (codigo) => process.exit(codigo ?? 0));
