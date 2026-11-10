import { armarFormularioPago } from '../api/backend';
import { ApiError, alSesionExpirada, fijarToken, pedir, urlDe } from '../api/cliente';

function responder(estado: number, cuerpo?: unknown): Response {
  return {
    ok: estado >= 200 && estado < 300,
    status: estado,
    json: async () => {
      if (cuerpo === undefined) {
        throw new Error('sin cuerpo');
      }
      return cuerpo;
    },
  } as Response;
}

describe('cliente HTTP', () => {
  const fetchOriginal = globalThis.fetch;

  afterEach(() => {
    globalThis.fetch = fetchOriginal;
    fijarToken(null);
    alSesionExpirada(null);
  });

  it('manda el token y devuelve el JSON', async () => {
    const simulado = jest.fn().mockResolvedValue(responder(200, { ok: true }));
    globalThis.fetch = simulado;
    fijarToken('abc');

    const datos = await pedir<{ ok: boolean }>('/algo');

    expect(datos).toEqual({ ok: true });
    const [, opciones] = simulado.mock.calls[0];
    expect(opciones.headers.Authorization).toBe('Bearer abc');
  });

  it('envía el cuerpo como JSON', async () => {
    const simulado = jest.fn().mockResolvedValue(responder(200, {}));
    globalThis.fetch = simulado;

    await pedir('/auth/login', { metodo: 'POST', cuerpo: { username: 'a' } });

    const [, opciones] = simulado.mock.calls[0];
    expect(opciones.method).toBe('POST');
    expect(opciones.headers['Content-Type']).toBe('application/json');
    expect(opciones.body).toBe('{"username":"a"}');
  });

  it('avisa que la sesión venció cuando el servidor responde 401', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue(responder(401));
    const aviso = jest.fn();
    alSesionExpirada(aviso);

    await expect(pedir('/mi-informacion/alumnos')).rejects.toMatchObject({ status: 401 });
    expect(aviso).toHaveBeenCalledTimes(1);
  });

  it('en el login un 401 es de credenciales y no cierra la sesión', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue(responder(401));
    const aviso = jest.fn();
    alSesionExpirada(aviso);

    await expect(pedir('/auth/login', { metodo: 'POST', sinSesion: true })).rejects.toThrow(
      'Usuario o contraseña incorrectos',
    );
    expect(aviso).not.toHaveBeenCalled();
  });

  it('conserva el mensaje y los errores por campo del servidor', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue(
      responder(400, { message: 'No se pudo registrar el pago', errores: { items: 'Hay ítems que ya fueron pagados' } }),
    );

    const error = (await pedir('/x').catch((e: unknown) => e)) as ApiError;

    expect(error).toBeInstanceOf(ApiError);
    expect(error.status).toBe(400);
    expect(error.errores.items).toBe('Hay ítems que ya fueron pagados');
    expect(error.textoCompleto()).toBe('No se pudo registrar el pago: Hay ítems que ya fueron pagados');
  });

  it('da un mensaje claro cuando no hay conexión', async () => {
    globalThis.fetch = jest.fn().mockRejectedValue(new TypeError('Network request failed'));

    await expect(pedir('/x')).rejects.toMatchObject({ status: 0 });
  });

  it('usa un mensaje general si el error no trae cuerpo', async () => {
    globalThis.fetch = jest.fn().mockResolvedValue(responder(403));

    await expect(pedir('/x')).rejects.toThrow('No tenés permiso para hacer esto');
  });
});

describe('urlDe', () => {
  it('omite los parámetros vacíos y codifica los demás', () => {
    const url = urlDe('/facturas', { desde: '2026-01-01', hasta: undefined, texto: 'a b' });

    expect(url).toContain('/facturas?desde=2026-01-01&texto=a%20b');
    expect(url).not.toContain('hasta');
  });

  it('no agrega signo de pregunta sin parámetros', () => {
    expect(urlDe('/facturas')).not.toContain('?');
  });
});

describe('armarFormularioPago', () => {
  it('repite itemIds por cada ítem y agrega fecha y archivo', () => {
    const formulario = armarFormularioPago([10, 12], '2026-12-15', {
      uri: 'file:///transferencia.pdf',
      name: 'transferencia.pdf',
      type: 'application/pdf',
    });
    expect(formulario.getAll('itemIds')).toEqual(['10', '12']);
    expect(formulario.get('fechaPago')).toBe('2026-12-15');
    expect(formulario.has('archivo')).toBe(true);
  });
});
