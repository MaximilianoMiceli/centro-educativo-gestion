/**
 * Prueba de punta a punta contra un backend real, usando las mismas funciones que la app.
 *
 *   EXPO_PUBLIC_API_URL=http://localhost:8080/api/v1 npm run test:integracion
 *
 * El backend tiene que tener los datos de demostración: usuarios admin, padre1 y alumno1 con clave "password".
 * Genera la facturación de un mes y registra un pago, así que conviene usar una base de pruebas.
 */
import assert from 'node:assert/strict';
import { after, describe, it } from 'node:test';
import { deuda, facturas, hijos, iniciarSesion, miInformacion, registrarPago } from '../src/api/backend';
import { ApiError, fijarToken, pedir } from '../src/api/cliente';

const PERIODO = '2027-03';
const hoy = () => new Date().toISOString().slice(0, 10);
const pdf = () => new Blob(['%PDF-1.4 transferencia de prueba'], { type: 'application/pdf' });

describe('la app contra el backend real', () => {
  after(() => fijarToken(null));

  it('el padre ve a sus hijos, sus facturas y su deuda, y registra un pago con comprobante', async () => {
    const admin = await iniciarSesion('admin', 'password');
    fijarToken(admin.token);
    await pedir(`/facturacion/generar`, { metodo: 'POST', parametros: { periodo: PERIODO } });

    const padre = await iniciarSesion('padre1', 'password');
    assert.equal(padre.rol, 'PADRE');
    fijarToken(padre.token);

    const lista = await hijos();
    assert.ok(lista.length >= 2, 'el padre debería tener al menos dos hijos');

    const factura = (await facturas(lista[0].id)).find((f) => f.periodo === PERIODO);
    assert.ok(factura, 'debería existir la factura del período');
    assert.equal(factura.estado, 'PENDIENTE');
    assert.ok(factura.items.length >= 1);

    const antes = await deuda(lista[0].id);
    assert.ok(antes.totalAdeudado >= factura.total);

    const cuota = factura.items.find((i) => i.concepto === 'CUOTA_BASE');
    assert.ok(cuota);
    const pago = await registrarPago(factura.id, [cuota.id], hoy(), { blob: pdf(), name: 'transferencia.pdf', type: 'application/pdf' });
    assert.match(pago.numeroComprobante, /^C-\d{4}-\d{6}$/);
    assert.deepEqual(pago.itemIds, [cuota.id]);

    const actualizada = (await facturas(lista[0].id)).find((f) => f.id === factura.id);
    assert.ok(actualizada);
    assert.equal(actualizada.pagos.length, 1);
    assert.equal(actualizada.items.find((i) => i.id === cuota.id)?.pagado, true);

    const despues = await deuda(lista[0].id);
    assert.ok(despues.totalAdeudado < antes.totalAdeudado, 'la deuda tiene que bajar después del pago');

    const info = await miInformacion(lista[0].id);
    assert.equal(info.servicios.alumnoId, lista[0].id);
  });

  it('un ítem ya pagado se rechaza con el detalle del error', async () => {
    fijarToken((await iniciarSesion('padre1', 'password')).token);
    const lista = await hijos();
    const factura = (await facturas(lista[0].id)).find((f) => f.periodo === PERIODO);
    const pagado = factura?.items.find((i) => i.pagado);
    assert.ok(factura && pagado, 'la prueba anterior debería haber dejado un ítem pagado');

    const error = await registrarPago(factura.id, [pagado.id], hoy(), { blob: pdf(), name: 'otra.pdf', type: 'application/pdf' })
      .catch((e: unknown) => e);

    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 400);
    assert.equal(error.errores.items, 'Hay ítems que ya fueron pagados');
  });

  it('un archivo que no es PDF ni imagen se rechaza', async () => {
    fijarToken((await iniciarSesion('padre1', 'password')).token);
    const lista = await hijos();
    const factura = (await facturas(lista[1].id)).find((f) => f.periodo === PERIODO);
    const pendiente = factura?.items.find((i) => !i.pagado);
    assert.ok(factura && pendiente, 'debería quedar un ítem pendiente');

    const texto = new Blob(['esto no es un comprobante'], { type: 'text/plain' });
    const error = await registrarPago(factura.id, [pendiente.id], hoy(), { blob: texto, name: 'nota.txt', type: 'text/plain' })
      .catch((e: unknown) => e);

    assert.ok(error instanceof ApiError);
    assert.equal(error.errores.archivo, 'Solo se admiten archivos PDF, PNG o JPG');
  });

  it('un alumno consulta sus facturas pero no puede pagar', async () => {
    fijarToken((await iniciarSesion('alumno1', 'password')).token);
    const propio = (await facturas((await hijos())[0].id)).find((f) => f.periodo === PERIODO);
    assert.ok(propio);

    const error = await registrarPago(propio.id, [propio.items[0].id], hoy(), { blob: pdf(), name: 'x.pdf', type: 'application/pdf' })
      .catch((e: unknown) => e);

    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 403);
  });

  it('las credenciales incorrectas dan un mensaje claro', async () => {
    const error = await iniciarSesion('padre1', 'incorrecta').catch((e: unknown) => e);

    assert.ok(error instanceof ApiError);
    assert.equal(error.status, 401);
    assert.equal(error.message, 'Usuario o contraseña incorrectos');
  });
});
