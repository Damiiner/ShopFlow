"use client";

import Link from "next/link";
import { useAuth } from "@/lib/auth";

export function Header() {
  const { user, logout } = useAuth();

  return (
    <header
      style={{
        background: "#fff",
        borderBottom: "1px solid var(--border)",
        position: "sticky",
        top: 0,
        zIndex: 40,
      }}
    >
      <div className="container" style={{ display: "flex", alignItems: "center", gap: "1.5rem", height: 64 }}>
        <Link href="/" style={{ fontWeight: 800, fontSize: "1.25rem", color: "var(--text)", textDecoration: "none" }}>
          ShopFlow
        </Link>
        <nav style={{ display: "flex", gap: "1rem", flex: 1, flexWrap: "wrap" }}>
          <Link href="/catalog">Catalogue</Link>
          {user?.role === "CUSTOMER" && (
            <>
              <Link href="/cart">Panier</Link>
              <Link href="/account">Mon compte</Link>
            </>
          )}
          {user?.role === "SELLER" && <Link href="/seller">Espace vendeur</Link>}
        </nav>
        <div style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
          {user ? (
            <>
              <span style={{ fontSize: "0.875rem", color: "var(--muted)" }}>{user.email}</span>
              <button type="button" className="btn btn-ghost" onClick={() => logout()}>
                Déconnexion
              </button>
            </>
          ) : (
            <>
              <Link href="/login" className="btn btn-ghost" style={{ textDecoration: "none" }}>
                Connexion
              </Link>
              <Link href="/register" className="btn btn-primary" style={{ textDecoration: "none" }}>
                Inscription
              </Link>
            </>
          )}
        </div>
      </div>
    </header>
  );
}
