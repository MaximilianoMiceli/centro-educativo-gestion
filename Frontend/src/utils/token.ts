function payloadDelToken(token: string): Record<string, unknown> {
  try {
    const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(base64));
  } catch {
    return {};
  }
}

export function rolDelToken(token: string): string | null {
  const rol = payloadDelToken(token).rol;
  return typeof rol === 'string' ? rol : null;
}

export function debeCambiarPasswordDelToken(token: string): boolean {
  return payloadDelToken(token).debeCambiarPassword === true;
}
