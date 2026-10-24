"use client";

import Image from "next/image";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { RatingStars } from "@/components/RatingStars";

type Variant = {
  id: number;
  attributeName: string;
  attributeValue: string;
  additionalStock: number;
  priceDelta: number;
};

type Detail = {
  id: number;
  name: string;
  description: string;
  price: number;
  promoPrice?: number | null;
  onPromo: boolean;
  discountPercent: number;
  stock: number;
  imageUrls: string[];
  variants: Variant[];
  averageRating?: number | null;
  reviewCount: number;
  shopName: string;
};

type Review = {
  id: number;
  rating: number;
  comment: string;
  authorName: string;
  createdAt: string;
};

export default function ProductPage() {
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const router = useRouter();
  const [p, setP] = useState<Detail | null>(null);
  const [reviews, setReviews] = useState<Review[]>([]);
  const [variantId, setVariantId] = useState<number | null>(null);
  const [qty, setQty] = useState(1);
  const [msg, setMsg] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Detail>(`/api/products/${id}`)
      .then(setP)
      .catch(() => setP(null));
    apiFetch<{ content: Review[] }>(`/api/reviews/product/${id}?size=5`)
      .then((r) => setReviews(r.content))
      .catch(() => setReviews([]));
  }, [id]);

  const addCart = async () => {
    setMsg(null);
    if (user?.role !== "CUSTOMER") {
      router.push("/login?next=" + encodeURIComponent(`/products/${id}`));
      return;
    }
    try {
      await apiFetch("/api/cart/items", {
        method: "POST",
        body: JSON.stringify({
          productId: Number(id),
          variantId: variantId,
          quantity: qty,
        }),
      });
      setMsg("Ajouté au panier");
      router.push("/cart");
    } catch (e) {
      setMsg(String((e as Error).message));
    }
  };

  if (!p) {
    return (
      <div className="container" style={{ padding: "2rem" }}>
        <div className="skeleton" style={{ height: 400 }} />
      </div>
    );
  }

  const img = p.imageUrls[0] ?? "https://picsum.photos/seed/x/800/600";
  const showPromo = p.onPromo && p.promoPrice != null;

  return (
    <div
      className="container"
      style={{
        padding: "2rem 0",
        display: "grid",
        gap: "2rem",
        gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))",
      }}
    >
      <div className="card" style={{ position: "relative", aspectRatio: "1", minHeight: 320 }}>
        <Image src={img} alt="" fill style={{ objectFit: "cover" }} unoptimized />
      </div>
      <div>
        <h1 style={{ marginTop: 0 }}>{p.name}</h1>
        <p style={{ color: "var(--muted)" }}>{p.shopName}</p>
        {p.averageRating != null && (
          <div style={{ margin: "0.5rem 0" }}>
            <RatingStars value={p.averageRating} />
            <span style={{ color: "var(--muted)", fontSize: "0.875rem" }}> ({p.reviewCount} avis)</span>
          </div>
        )}
        <div style={{ display: "flex", alignItems: "baseline", gap: 12, margin: "1rem 0" }}>
          {showPromo ? (
            <>
              <span style={{ fontSize: "1.75rem", fontWeight: 800 }}>{Number(p.promoPrice).toFixed(2)} €</span>
              <span style={{ textDecoration: "line-through", color: "var(--muted)" }}>{Number(p.price).toFixed(2)} €</span>
              <span className="badge" style={{ background: "#dc2626", color: "#fff" }}>
                -{p.discountPercent}%
              </span>
            </>
          ) : (
            <span style={{ fontSize: "1.75rem", fontWeight: 800 }}>{Number(p.price).toFixed(2)} €</span>
          )}
        </div>
        <p style={{ whiteSpace: "pre-wrap" }}>{p.description}</p>

        {p.variants.length > 0 && (
          <div style={{ margin: "1rem 0" }}>
            <span className="label">Variante</span>
            <select
              className="input"
              value={variantId ?? ""}
              onChange={(e) => setVariantId(e.target.value ? Number(e.target.value) : null)}
            >
              <option value="">— Choisir —</option>
              {p.variants.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.attributeName}: {v.attributeValue}
                </option>
              ))}
            </select>
          </div>
        )}

        <div style={{ display: "flex", gap: "0.75rem", alignItems: "center", marginTop: "1rem" }}>
          <input
            type="number"
            min={1}
            className="input"
            style={{ width: 80 }}
            value={qty}
            onChange={(e) => setQty(Math.max(1, Number(e.target.value)))}
          />
          <button type="button" className="btn btn-primary" onClick={addCart}>
            Ajouter au panier
          </button>
        </div>
        {msg && <p style={{ marginTop: "0.75rem", color: msg.includes("Ajouté") ? "var(--success)" : "#b91c1c" }}>{msg}</p>}

        <h3 style={{ marginTop: "2rem" }}>Avis</h3>
        {reviews.length === 0 && <p style={{ color: "var(--muted)" }}>Pas encore d&apos;avis approuvés.</p>}
        {reviews.map((r) => (
          <div key={r.id} style={{ borderTop: "1px solid var(--border)", paddingTop: "0.75rem", marginTop: "0.75rem" }}>
            <RatingStars value={r.rating} />
            <div style={{ fontSize: "0.85rem", color: "var(--muted)" }}>{r.authorName}</div>
            <p>{r.comment}</p>
          </div>
        ))}
      </div>
    </div>
  );
}
