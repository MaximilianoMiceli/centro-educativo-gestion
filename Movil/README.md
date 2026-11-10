# Aplicación móvil

App para padres y alumnos de "Educar Para Transformar", hecha con React Native y Expo (TypeScript).
Consume la API REST del sistema de gestión (`Backend/`) con el mismo token JWT que la web: no tiene base de datos propia.

## Qué permite

- Ingresar y cerrar sesión. Si la cuenta tiene contraseña provisoria, obliga a cambiarla. También se puede cambiar desde el perfil.
- Elegir de qué hijo ver la información (si hay más de uno).
- **Cuotas:** facturas del mes con total, saldo, vencimiento y estado. Filtro por estado y por rango de fechas.
- **Detalle de la factura:** ítems (cuota, deportes, transporte, comedor), pagos con sus comprobantes, factura y comprobantes en PDF.
- **Informar un pago** (solo padres): se eligen los ítems pendientes, se indica la fecha y se adjunta la transferencia (PDF o imagen). El sistema emite un comprobante.
- **Historial de deuda:** cada ítem de pago con su estado (pagado, pendiente, vencido) y los totales.
- **Servicios del alumno:** deportes con horario, transporte y comedor, en modo consulta.

Los alumnos ven lo mismo, pero no pueden informar pagos. Los demás roles no pueden ingresar a la app.

## Cómo correrla

Requisitos: Node 20 o más nuevo y el backend funcionando.

```bash
cd Movil
npm install
npm start
```

En la terminal de Expo, `a` abre el emulador de Android. Hace falta Android Studio con un dispositivo virtual creado.

El emulador ve la PC como `10.0.2.2`, y esa es la dirección por defecto: `http://10.0.2.2:8080/api/v1`.
Para usar otro backend se define la variable antes de iniciar:

```bash
EXPO_PUBLIC_API_URL=https://centro-educativo-gestion.onrender.com/api/v1 npm start
```

La versión desplegada en Render necesita tener las migraciones V18 y V19 para que las pantallas de cuotas funcionen.

## Probarla en un celular con Expo Go

Con el backend corriendo en la PC (puerto 8080) y el celular en la misma red wifi:

```bash
npm run celular
```

El script detecta la dirección de la PC en la red local, la usa como servidor de la API y muestra un código QR. Se escanea con la cámara (iPhone) o desde Expo Go (Android).
La primera vez, el firewall de Windows tiene que permitir los puertos 8080 (backend) y 8081 (Expo) en la red privada. Si el celular no conecta, suele ser eso.
El backend tiene que permitir el acceso desde el celular: no necesita CORS, pero sí escuchar en todas las interfaces (lo hace por defecto).

## Estructura

```
Movil/
├── App.tsx                 punto de entrada: proveedores y navegación
├── src/
│   ├── api/                cliente HTTP, tipos y una función por endpoint
│   ├── auth/               sesión y guardado seguro del token
│   ├── familia/            hijos del usuario y cuál está elegido
│   ├── navegacion/         pestañas y pantallas según la sesión
│   ├── pantallas/          ingreso, cuotas, factura, deuda, servicios, perfil
│   ├── componentes/        botones, campos y tarjetas compartidos
│   ├── utils/              formato, token, cálculos de facturas, descarga de PDF
│   └── __tests__/          pruebas unitarias
└── scripts/integracion.test.ts   prueba contra un backend real
```

## Pruebas

```bash
npm test                 # pruebas unitarias (Jest)
npm run typecheck        # verificación de tipos
EXPO_PUBLIC_API_URL=http://localhost:8080/api/v1 npm run test:integracion
```

La prueba de integración usa las mismas funciones de la app contra un backend real: ingresa como padre, ve a sus hijos y facturas,
registra un pago con comprobante y verifica los rechazos (ítem ya pagado, archivo inválido, alumno intentando pagar).
Necesita los datos de demostración y genera facturación, así que conviene correrla contra una base de pruebas.
