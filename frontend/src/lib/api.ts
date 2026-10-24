const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

const ACCESS = "shopflow_access";
const REFRESH = "shopflow_refresh";

export function getAccessToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(ACCESS);
}

export function setTokens(access: string, refresh: string) {
  localStorage.setItem(ACCESS, access);
  localStorage.setItem(REFRESH, refresh);
}

export function clearTokens() {
  localStorage.removeItem(ACCESS);
  localStorage.removeItem(REFRESH);
}

async function tryRefresh(): Promise<boolean> {
  const r = typeof window !== "undefined" ? localStorage.getItem(REFRESH) : null;
  if (!r) return false;
  const res = await fetch(`${API_BASE}/api/auth/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: r }),
  });
  if (!res.ok) {
    clearTokens();
    return false;
  }
  const data = await res.json();
  setTokens(data.accessToken, data.refreshToken);
  return true;
}

export async function apiFetch<T>(
  path: string,
  init: RequestInit = {},
  retry = true
): Promise<T> {
  const headers = new Headers(init.headers);
  if (!headers.has("Content-Type") && init.body && typeof init.body === "string") {
    headers.set("Content-Type", "application/json");
  }
  const token = getAccessToken();
  if (token) headers.set("Authorization", `Bearer ${token}`);

  const res = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (res.status === 401 && retry && typeof window !== "undefined" && localStorage.getItem(ACCESS)) {
    const ok = await tryRefresh();
    if (ok) return apiFetch<T>(path, init, false);
  }
  if (!res.ok) {
    let msg = res.statusText;
    try {
      const j = await res.json();
      msg = j.error ?? JSON.stringify(j);
    } catch {
      /* ignore */
    }
    throw new Error(msg);
  }
  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

export { API_BASE };

export type PageResponse<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
};
