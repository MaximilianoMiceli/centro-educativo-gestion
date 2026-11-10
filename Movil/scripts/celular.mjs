// Inicia Expo para probar la app desde un celular con Expo Go.
// Detecta la dirección de esta PC en la red local y la usa como servidor de la API,
// así no hay que escribirla a mano (cambia cuando se cambia de red).
// Uso: npm run celular            (opcional: PUERTO_API=8080)
//      npm run celular -- --dry   (solo muestra la dirección detectada)
//      Si detecta mal la red: $env:IP_API="192.168.x.x"; npm run celular
import { spawn } from 'node:child_process';
import { networkInterfaces } from 'node:os';

const puertoApi = process.env.PUERTO_API ?? '8080';

/** Una dirección de red local típica (192.168.x.x, 10.x.x.x, 172.16-31.x.x). Se descartan VPN como Tailscale (100.x). */
function esRedLocal(ip) {
  if (ip.startsWith('192.168.') || ip.startsWith('10.')) return true;
  const m = /^172\.(\d+)\./.exec(ip);
  return m !== null && Number(m[1]) >= 16 && Number(m[1]) <= 31;
}

/** Adaptadores virtuales (Docker, WSL, Hyper-V, VPN) que el celular no puede alcanzar. */
const VIRTUAL = /vethernet|wsl|hyper-v|virtual|vmware|vbox|docker|tailscale|vpn|loopback|bluetooth/i;

function prioridad(ip) {
  if (ip.startsWith('192.168.')) return 0;
  if (ip.startsWith('10.')) return 1;
  return 2;
}

function direccionLocal() {
  const candidatas = [];
  for (const [nombre, direcciones] of Object.entries(networkInterfaces())) {
    if (VIRTUAL.test(nombre)) continue;
    for (const i of direcciones ?? []) {
      if (i.family === 'IPv4' && !i.internal && esRedLocal(i.address)) candidatas.push({ nombre, ip: i.address });
    }
  }
  candidatas.sort((a, b) => prioridad(a.ip) - prioridad(b.ip));
  if (candidatas.length > 1) {
    console.log('Redes encontradas: ' + candidatas.map((c) => c.nombre + ' ' + c.ip).join(', '));
  }
  return candidatas[0]?.ip;
}

const ip = process.env.IP_API ?? direccionLocal();
if (!ip) {
  console.error('No se encontró una dirección de red local. Conectá la PC a la misma red wifi que el celular.');
  process.exit(1);
}

const api = `http://${ip}:${puertoApi}/api/v1`;
console.log(`\nLa app del celular va a usar el servidor: ${api}`);
console.log('El celular y la PC tienen que estar en la misma red wifi.\n');

if (process.argv.includes('--dry')) process.exit(0);

const proceso = spawn('npx', ['expo', 'start', '--go', '--offline'], {
  stdio: 'inherit',
  shell: true,
  env: { ...process.env, EXPO_PUBLIC_API_URL: api },
});
proceso.on('exit', (codigo) => process.exit(codigo ?? 0));
