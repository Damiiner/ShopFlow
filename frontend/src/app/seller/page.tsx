"use client";

import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { RequireSeller } from "@/components/RequireSeller";

type CategoryNode = { id: number; name: string; children: CategoryNode[] };
type SellerStats = {
  revenue: number;
  pendingOrdersCount: number;
  lowStockAlerts: { name: string; stock: number }[];
};

function SellerInner() {
  const [data, setData] = useState<SellerStats | null>(null);
  const [categories, setCategories] = useState<CategoryNode[]>([]);
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [price, setPrice] = useState("");
  const [stock, setStock] = useState("");
  const [promoPrice, setPromoPrice] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [uploadDataUrl, setUploadDataUrl] = useState<string | null>(null);
  const [formErr, setFormErr] = useState<string | null>(null);
  const [formOk, setFormOk] = useState<string | null>(null);

  useEffect(() => {
    apiFetch<SellerStats>("/api/dashboard/seller").then(setData);
    apiFetch<CategoryNode[]>("/api/categories").then(setCategories);
  }, []);

  const flatCategories = categories.flatMap(function flatten(c): { id: number; name: string }[] {
    return [{ id: c.id, name: c.name }, ...c.children.flatMap(flatten)];
  });

  const onPickFile = (file: File | null) => {
    if (!file) {
      setUploadDataUrl(null);
      return;
    }
    const reader = new FileReader();
    reader.onload = () => setUploadDataUrl(String(reader.result));
    reader.readAsDataURL(file);
  };

  const submitProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormErr(null);
    setFormOk(null);
    try {
      const parsedPrice = Number(price);
      const parsedStock = Number(stock);
      const parsedPromo = promoPrice.trim() ? Number(promoPrice) : null;
      if (!Number.isFinite(parsedPrice) || parsedPrice <= 0) {
        throw new Error("Prix invalide.");
      }
      if (!Number.isInteger(parsedStock) || parsedStock < 0) {
        throw new Error("Quantité invalide.");
      }
      if (!categoryId) {
        throw new Error("Choisissez une catégorie.");
      }
      const images: string[] = [];
      if (imageUrl.trim()) images.push(imageUrl.trim());
      if (uploadDataUrl) images.push(uploadDataUrl);
      await apiFetch("/api/products", {
        method: "POST",
        body: JSON.stringify({
          name: name.trim(),
          description: description.trim(),
          price: parsedPrice,
          promoPrice: parsedPromo,
          stock: parsedStock,
          categoryIds: [Number(categoryId)],
          imageUrls: images,
          variants: [],
        }),
      });
      setFormOk("Produit ajouté avec succès.");
      setName("");
      setDescription("");
      setPrice("");
      setStock("");
      setPromoPrice("");
      setImageUrl("");
      setUploadDataUrl(null);
      apiFetch<SellerStats>("/api/dashboard/seller").then(setData);
    } catch (e) {
      setFormErr(String((e as Error).message));
    }
  };

  return (
    <div className="container" style={{ padding: "2rem 0" }}>
      <h1>Espace vendeur</h1>
      {!data ? (
        <div className="skeleton" style={{ height: 160 }} />
      ) : (
        <div className="card" style={{ padding: "1.5rem", maxWidth: 540 }}>
          <p>
            <strong>Chiffre d&apos;affaires (hors annulées) :</strong> {String(data.revenue)} EUR
          </p>
          <p>
            <strong>Commandes à traiter (approx.) :</strong> {String(data.pendingOrdersCount)}
          </p>
          <h3 style={{ marginTop: "1rem" }}>Alertes stock bas (&lt; 5)</h3>
          {data.lowStockAlerts.length === 0 ? (
            <p style={{ color: "var(--muted)" }}>RAS</p>
          ) : (
            <ul>
              {data.lowStockAlerts.map((x, i) => (
                <li key={i}>
                  {x.name} — {x.stock} unités
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
      <div className="card" style={{ marginTop: "1.5rem", padding: "1.5rem", maxWidth: 700 }}>
        <h2 style={{ marginTop: 0 }}>Ajouter un produit</h2>
        <form onSubmit={submitProduct} style={{ display: "grid", gap: "0.9rem" }}>
          <div>
            <label className="label">Nom du produit</label>
            <input className="input" value={name} onChange={(e) => setName(e.target.value)} required />
          </div>
          <div>
            <label className="label">Description</label>
            <textarea className="input" rows={3} value={description} onChange={(e) => setDescription(e.target.value)} />
          </div>
          <div style={{ display: "grid", gap: "0.9rem", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))" }}>
            <div>
              <label className="label">Prix</label>
              <input className="input" type="number" min="0.01" step="0.01" value={price} onChange={(e) => setPrice(e.target.value)} required />
            </div>
            <div>
              <label className="label">Quantité</label>
              <input className="input" type="number" min="0" step="1" value={stock} onChange={(e) => setStock(e.target.value)} required />
            </div>
            <div>
              <label className="label">Prix promo (optionnel)</label>
              <input className="input" type="number" min="0" step="0.01" value={promoPrice} onChange={(e) => setPromoPrice(e.target.value)} />
            </div>
          </div>
          <div>
            <label className="label">Catégorie</label>
            <select className="input" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required>
              <option value="">-- Choisir --</option>
              {flatCategories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="label">Image (URL)</label>
            <input className="input" type="url" placeholder="https://..." value={imageUrl} onChange={(e) => setImageUrl(e.target.value)} />
          </div>
          <div>
            <label className="label">Image (upload)</label>
            <input className="input" type="file" accept="image/*" onChange={(e) => onPickFile(e.target.files?.[0] ?? null)} />
          </div>
          {formErr && <p style={{ margin: 0, color: "#b91c1c" }}>{formErr}</p>}
          {formOk && <p style={{ margin: 0, color: "var(--success)" }}>{formOk}</p>}
          <button type="submit" className="btn btn-primary" style={{ width: "fit-content" }}>
            Ajouter le produit
          </button>
        </form>
      </div>
    </div>
  );
}

export default function SellerPage() {
  return (
    <RequireSeller>
      <SellerInner />
    </RequireSeller>
  );
}
