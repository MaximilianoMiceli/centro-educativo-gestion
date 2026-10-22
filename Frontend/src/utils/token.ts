export function rolDelToken(token: string): string | null {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    return typeof payload.rol === 'string' ? payload.rol : null;
  } catch {
    return null;
  }
}
