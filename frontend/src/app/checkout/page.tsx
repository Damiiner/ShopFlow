"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { RequireCustomer } from "@/components/RequireCustomer";

type Address = {
  id: number;
  street: string;
  city: string;
  postalCode: string;
  country: string;
  principal: boolean;
};

function CheckoutInner() {
  const router = useRouter();
  const [addresses, setAddresses] = useState<Address[]>([]);
  const [sel, setSel] = useState<number | null>(null);
  const [err, setErr] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<Address[]>("/api/addresses")
      .then((a) => {
        setAddresses(a);
        const def = a.find((x) => x.principal) ?? a[0];
        if (def) setSel(def.id);
      })
      .catch(() => setErr("Impossible de charger les adresses"));
  }, []);

  const submit = async () => {
    if (!sel) {
      setErr("Choisissez une adresse");
      return;
    }
    setErr(null);
    try {
      const order = await apiFetch<{ id: number; orderNumber: string }>("/api/orders", {
        method: "POST",
        body: JSON.stringify({ addressId: sel }),
      });
      router.push(`/account?placed=${order.orderNumber}`);
    } catch (e) {
      setErr(String((e as Error).message));
    }
  };

  return (
    <div className="container" style={{ padding: "2rem 0", maxWidth: 560 }}>
      <h1>Commande</h1>
      <p style={{ color: "var(--muted)" }}>Sélectionnez une adresse de livraison (paiement simulé : commande marquée payée).</p>
      {addresses.length === 0 && (
        <p>
          Aucune adresse. <Link href="/account">Ajoutez une adresse dans Mon compte</Link> (à implémenter via API) — compte démo
          inclut déjà une adresse pour <code>customer@shopflow.com</code>.
        </p>
      )}
      <ul style={{ listStyle: "none", padding: 0 }}>
        {addresses.map((a) => (
          <li key={a.id} className="card" style={{ padding: "1rem", marginBottom: "0.5rem" }}>
            <label style={{ display: "flex", gap: "0.75rem", cursor: "pointer" }}>
              <input type="radio" name="addr" checked={sel === a.id} onChange={() => setSel(a.id)} />
              <span>
                {a.street}, {a.postalCode} {a.city}, {a.country}
                {a.principal && <span className="badge" style={{ marginLeft: 8 }}>Par défaut</span>}
              </span>
            </label>
          </li>
        ))}
      </ul>
      {err && <p style={{ color: "#b91c1c" }}>{err}</p>}
      <button type="button" className="btn btn-primary" onClick={submit} disabled={!sel}>
        Confirmer la commande
      </button>
    </div>
  );
}

export default function CheckoutPage() {
  return (
    <RequireCustomer>
      <CheckoutInner />
    </RequireCustomer>
  );
}
