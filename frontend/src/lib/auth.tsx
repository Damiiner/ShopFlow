"use client";

import React, { createContext, useCallback, useContext, useEffect, useState } from "react";
import { apiFetch, clearTokens, getAccessToken, setTokens } from "./api";

type Role = "ADMIN" | "SELLER" | "CUSTOMER";

type User = { email: string; role: Role };

function parseJwt(token: string): { email?: string; role?: string } | null {
  try {
    const p = token.split(".")[1];
    const json = JSON.parse(atob(p));
    return { email: json.email, role: json.role };
  } catch {
    return null;
  }
}

type AuthContextType = {
  user: User | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (body: Record<string, unknown>) => Promise<void>;
  logout: () => Promise<void>;
  refreshUser: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(() => {
    const t = getAccessToken();
    if (!t) {
      setUser(null);
      return;
    }
    const c = parseJwt(t);
    if (c?.email && c?.role) {
      setUser({ email: c.email, role: c.role as Role });
    } else setUser(null);
  }, []);

  useEffect(() => {
    refreshUser();
    setLoading(false);
  }, [refreshUser]);

  const login = async (email: string, password: string) => {
    const data = await apiFetch<{
      accessToken: string;
      refreshToken: string;
    }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    setTokens(data.accessToken, data.refreshToken);
    refreshUser();
  };

  const register = async (body: Record<string, unknown>) => {
    const data = await apiFetch<{
      accessToken: string;
      refreshToken: string;
    }>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(body),
    });
    setTokens(data.accessToken, data.refreshToken);
    refreshUser();
  };

  const logout = async () => {
    const refresh = typeof window !== "undefined" ? localStorage.getItem("shopflow_refresh") : null;
    if (refresh) {
      try {
        await apiFetch("/api/auth/logout", {
          method: "POST",
          body: JSON.stringify({ refreshToken: refresh }),
        });
      } catch {
        /* ignore */
      }
    }
    clearTokens();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth outside provider");
  return ctx;
}
