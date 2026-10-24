"use client";

import Link from "next/link";
import { useSearchParams } from "next/navigation";
import { Suspense, useEffect, useState } from "react";
import { apiFetch, PageResponse } from "@/lib/api";
import { RequireCustomer } from "@/components/RequireCustomer";
import { OrderStatusBadge } from "@/components/OrderStatusBadge";

type Order = {
  id: number;
  orderNumber: string;
  status: string;
  totalTtc: number;
  orderedAt: string;
};

function AccountInner() {
  const sp = useSearchParams();
  const placed = sp.get("placed");
  const [orders, setOrders] = useState<Order[] | null>(null);
  const [dash, setDash] = useState<Record<string, unknown> | null>(null);

  useEffect(() => {
    apiFetch<PageResponse<Order>>("/api/orders/my?size=20").then((p) => setOrders(p.content));
    apiFetch<Record<string, unknown>>("/api/dashboard/customer").then(setDash);
  }, []);

  return (
    <div className="container" style={{ padding: "2rem 0" }}>
      <h1>Mon compte</h1>
      {placed && (
        <p style={{ background: "#d1fae5", padding: "0.75rem 1rem", borderRadius: 10 }}>
          Commande confirmée : <strong>{placed}</strong>
        </p>
      )}
      <section style={{ marginTop: "2rem" }}>
        <h2>Commandes en cours</h2>
        {dash?.activeOrders && Array.isArray(dash.activeOrders) && (dash.activeOrders as object[]).length > 0 ? (
          <ul>
            {(dash.activeOrders as { orderNumber: string; status: string }[]).map((o, i) => (
              <li key={i}>
                {o.orderNumber} — <OrderStatusBadge status={o.status} />
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ color: "var(--muted)" }}>Aucune commande active.</p>
        )}
      </section>
      <section style={{ marginTop: "2rem" }}>
        <h2>Historique</h2>
        {!orders ? (
          <div className="skeleton" style={{ height: 120 }} />
        ) : orders.length === 0 ? (
          <p>
            Aucune commande. <Link href="/catalog">Catalogue</Link>
          </p>
        ) : (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr style={{ textAlign: "left", borderBottom: "1px solid var(--border)" }}>
                <th style={{ padding: "0.5rem" }}>N°</th>
                <th>Statut</th>
                <th>Total</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((o) => (
                <tr key={o.id} style={{ borderBottom: "1px solid var(--border)" }}>
                  <td style={{ padding: "0.5rem" }}>{o.orderNumber}</td>
                  <td>
                    <OrderStatusBadge status={o.status} />
                  </td>
                  <td>{Number(o.totalTtc).toFixed(2)} €</td>
                  <td style={{ fontSize: "0.85rem", color: "var(--muted)" }}>{new Date(o.orderedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
      <p style={{ marginTop: "2rem", color: "var(--muted)", fontSize: "0.9rem" }}>
        Les adresses sont gérées via l&apos;API <code>/api/addresses</code> (Postman ou extension) — le compte Test inclut une adresse
        Tunis.
      </p>
    </div>
  );
}

export default function AccountPage() {
  return (
    <RequireCustomer>
      <Suspense fallback={null}>
        <AccountInner />
      </Suspense>
    </RequireCustomer>
  );
}
