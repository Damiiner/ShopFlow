"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useState, Suspense } from "react";
import { apiFetch, PageResponse } from "@/lib/api";
import { ProductCard, ProductSummary } from "@/components/ProductCard";

function CatalogInner() {
  const sp = useSearchParams();
  const categoryId = sp.get("categoryId");
  const q = sp.get("q");
  const [page, setPage] = useState(0);
  const [data, setData] = useState<PageResponse<ProductSummary> | null>(null);
  const [loading, setLoading] = useState(true);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    setLoading(true);
    const path = q
      ? `/api/products/search?q=${encodeURIComponent(q)}&page=${page}&size=12&sort=createdAt,desc`
      : `/api/products?page=${page}&size=12&sort=createdAt,desc${categoryId ? `&categoryId=${categoryId}` : ""}`;
    apiFetch<PageResponse<ProductSummary>>(path)
      .then(setData)
      .catch((e) => setErr(e.message))
      .finally(() => setLoading(false));
  }, [categoryId, q, page]);

  return (
    <div className="container" style={{ padding: "2rem 0" }}>
      <h1 style={{ marginBottom: "1.5rem" }}>Catalogue</h1>
      <form
        action="/catalog"
        method="get"
        style={{ display: "flex", gap: "0.75rem", marginBottom: "1.5rem", flexWrap: "wrap" }}
      >
        <input name="q" className="input" placeholder="Recherche…" defaultValue={q ?? ""} style={{ maxWidth: 320 }} />
        {categoryId && <input type="hidden" name="categoryId" value={categoryId} />}
        <button type="submit" className="btn btn-primary">
          Rechercher
        </button>
      </form>
      {err && <p style={{ color: "#b91c1c" }}>{err}</p>}
      {loading && <div className="skeleton" style={{ height: 300 }} />}
      {data && (
        <>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fill, minmax(240px, 1fr))",
              gap: "1.25rem",
            }}
          >
            {data.content.map((p) => (
              <ProductCard key={p.id} p={p} />
            ))}
          </div>
          <div style={{ display: "flex", gap: "1rem", marginTop: "2rem", justifyContent: "center" }}>
            <button type="button" className="btn btn-ghost" disabled={page <= 0} onClick={() => setPage((p) => p - 1)}>
              Précédent
            </button>
            <span style={{ alignSelf: "center", color: "var(--muted)" }}>
              Page {data.number + 1} / {Math.max(1, data.totalPages)}
            </span>
            <button
              type="button"
              className="btn btn-ghost"
              disabled={page >= data.totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Suivant
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default function CatalogPage() {
  return (
    <Suspense fallback={<div className="container skeleton" style={{ height: 400, margin: "2rem auto" }} />}>
      <CatalogInner />
    </Suspense>
  );
}
