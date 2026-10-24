"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { ProductCard, ProductSummary } from "@/components/ProductCard";

type CategoryNode = { id: number; name: string; children: CategoryNode[] };

export default function HomePage() {
  const [top, setTop] = useState<ProductSummary[] | null>(null);
  const [cats, setCats] = useState<CategoryNode[] | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      apiFetch<ProductSummary[]>("/api/products/top-selling"),
      apiFetch<CategoryNode[]>("/api/categories"),
    ])
      .then(([t, c]) => {
        setTop(t);
        setCats(c);
      })
      .catch((e) => setErr(String(e.message)));
  }, []);

  return (
    <div>
      <section
        style={{
          background: "linear-gradient(135deg, #1e3a8a 0%, #3b82f6 50%, #60a5fa 100%)",
          color: "#fff",
          padding: "3rem 0",
        }}
      >
        <div className="container">
          <h1 style={{ fontSize: "2rem", margin: "0 0 0.5rem", fontWeight: 800 }}>Bienvenue sur ShopFlow</h1>
          <p style={{ opacity: 0.95, maxWidth: 520, marginBottom: "1.5rem" }}>
            Catalogue, panier persistant et commandes — API Spring Boot + Next.js.
          </p>
          <Link href="/catalog" className="btn btn-primary" style={{ background: "#fff", color: "#1e40af" }}>
            Voir le catalogue
          </Link>
        </div>
      </section>

      <div className="container" style={{ padding: "2.5rem 0" }}>
        {err && (
          <p style={{ color: "#b91c1c", marginBottom: "1rem" }}>
            API indisponible : démarrez le backend sur le port 8080. ({err})
          </p>
        )}
        <h2 style={{ fontSize: "1.35rem", marginBottom: "1rem" }}>Catégories</h2>
        {cats == null ? (
          <div className="skeleton" style={{ height: 40, maxWidth: 400, marginBottom: "2rem" }} />
        ) : (
          <div style={{ display: "flex", flexWrap: "wrap", gap: "0.75rem", marginBottom: "2.5rem" }}>
            {cats.flatMap(function flatten(c): { id: number; name: string }[] {
              return [{ id: c.id, name: c.name }, ...c.children.flatMap(flatten)];
            }).map((c) => (
              <Link key={c.id} href={`/catalog?categoryId=${c.id}`} className="badge" style={{ padding: "0.4rem 0.9rem" }}>
                {c.name}
              </Link>
            ))}
          </div>
        )}

        <h2 style={{ fontSize: "1.35rem", marginBottom: "1rem" }}>Produits populaires</h2>
        {top == null ? (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "1.25rem" }}>
            {[1, 2, 3].map((i) => (
              <div key={i} className="skeleton" style={{ height: 320 }} />
            ))}
          </div>
        ) : (
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))", gap: "1.25rem" }}>
            {top.map((p) => (
              <ProductCard key={p.id} p={p} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
