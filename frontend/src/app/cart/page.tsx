"use client";

import Image from "next/image";
import Link from "next/link";
import { useCallback, useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { RequireCustomer } from "@/components/RequireCustomer";

type Line = {
  itemId: number;
  productId: number;
  productName: string;
  variantId: number | null;
  variantLabel: string | null;
  quantity: number;
  maxQuantity: number;
  unitPrice: number;
  lineTotal: number;
  imageUrl: string | null;
};

type Cart = {
  cartId: number | null;
  lines: Line[];
  subtotal: number;
  discount: number;
  shippingFee: number;
  totalTtc: number;
  couponCode: string | null;
};

function CartInner() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [coupon, setCoupon] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const load = useCallback(() => {
    apiFetch<Cart>("/api/cart")
      .then(setCart)
      .catch((e) => setErr(e.message));
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const updateQty = async (itemId: number, quantity: number) => {
    await apiFetch(`/api/cart/items/${itemId}`, {
      method: "PUT",
      body: JSON.stringify({ quantity }),
    });
    load();
  };

  const remove = async (itemId: number) => {
    await apiFetch(`/api/cart/items/${itemId}`, { method: "DELETE" });
    load();
  };

  const applyCoupon = async () => {
    await apiFetch("/api/cart/coupon", {
      method: "POST",
      body: JSON.stringify({ code: coupon }),
    });
    load();
  };

  const removeCoupon = async () => {
    await apiFetch("/api/cart/coupon", { method: "DELETE" });
    load();
  };

  if (!cart) {
    return err ? <p style={{ color: "#b91c1c" }}>{err}</p> : <div className="skeleton" style={{ height: 300 }} />;
  }

  return (
    <div className="container" style={{ padding: "2rem 0" }}>
      <h1>Panier</h1>
      {cart.lines.length === 0 ? (
        <p>
          Votre panier est vide. <Link href="/catalog">Continuer les achats</Link>
        </p>
      ) : (
        <div style={{ display: "grid", gap: "2rem", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))" }}>
          <div>
            {cart.lines.map((l) => (
              <div
                key={l.itemId}
                className="card"
                style={{ display: "flex", gap: "1rem", padding: "1rem", marginBottom: "0.75rem", alignItems: "center" }}
              >
                <div style={{ position: "relative", width: 88, height: 88, flexShrink: 0, background: "#eee" }}>
                  <Image
                    src={l.imageUrl ?? "https://picsum.photos/seed/c/200"}
                    alt=""
                    fill
                    style={{ objectFit: "cover" }}
                    unoptimized
                  />
                </div>
                <div style={{ flex: 1 }}>
                  <Link href={`/products/${l.productId}`} style={{ fontWeight: 700 }}>
                    {l.productName}
                  </Link>
                  {l.variantLabel && <div style={{ fontSize: "0.85rem", color: "var(--muted)" }}>{l.variantLabel}</div>}
                  <div style={{ marginTop: 8 }}>
                    <input
                      type="number"
                      className="input"
                      style={{ width: 72 }}
                      min={1}
                      max={l.maxQuantity}
                      value={l.quantity}
                      onChange={(e) => updateQty(l.itemId, Number(e.target.value))}
                    />
                    <button type="button" className="btn btn-ghost" style={{ marginLeft: 8 }} onClick={() => remove(l.itemId)}>
                      Retirer
                    </button>
                  </div>
                </div>
                <div style={{ fontWeight: 700 }}>{Number(l.lineTotal).toFixed(2)} €</div>
              </div>
            ))}
          </div>
          <div className="card" style={{ padding: "1.25rem", height: "fit-content" }}>
            <h3 style={{ marginTop: 0 }}>Récapitulatif</h3>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
              <span>Sous-total</span>
              <span>{Number(cart.subtotal).toFixed(2)} €</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
              <span>Remise</span>
              <span>-{Number(cart.discount).toFixed(2)} €</span>
            </div>
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
              <span>Livraison</span>
              <span>{Number(cart.shippingFee).toFixed(2)} €</span>
            </div>
            <hr style={{ border: "none", borderTop: "1px solid var(--border)" }} />
            <div style={{ display: "flex", justifyContent: "space-between", fontWeight: 800, marginTop: 12 }}>
              <span>Total TTC</span>
              <span>{Number(cart.totalTtc).toFixed(2)} €</span>
            </div>
            <div style={{ marginTop: "1rem" }}>
              <input
                className="input"
                placeholder="Code promo (ex: TESTSHOP)"
                value={coupon}
                onChange={(e) => setCoupon(e.target.value)}
              />
              <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
                <button type="button" className="btn btn-ghost" onClick={applyCoupon}>
                  Appliquer
                </button>
                {cart.couponCode && (
                  <button type="button" className="btn btn-ghost" onClick={removeCoupon}>
                    Retirer ({cart.couponCode})
                  </button>
                )}
              </div>
            </div>
            <Link href="/checkout" className="btn btn-primary" style={{ width: "100%", marginTop: "1rem", textDecoration: "none" }}>
              Commander
            </Link>
          </div>
        </div>
      )}
    </div>
  );
}

export default function CartPage() {
  return (
    <RequireCustomer>
      <CartInner />
    </RequireCustomer>
  );
}
