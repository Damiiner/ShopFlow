import Image from "next/image";
import Link from "next/link";
import { RatingStars } from "./RatingStars";

export type ProductSummary = {
  id: number;
  name: string;
  price: number;
  promoPrice?: number | null;
  onPromo: boolean;
  discountPercent: number;
  mainImageUrl?: string | null;
  averageRating?: number | null;
  shopName?: string | null;
};

export function ProductCard({ p }: { p: ProductSummary }) {
  const img = p.mainImageUrl ?? "https://picsum.photos/seed/p/400/300";
  const showPromo = p.onPromo && p.promoPrice != null;

  return (
    <Link href={`/products/${p.id}`} className="card" style={{ textDecoration: "none", color: "inherit" }}>
      <div style={{ position: "relative", aspectRatio: "4/3", background: "#f0f0f0" }}>
        <Image src={img} alt="" fill sizes="280px" style={{ objectFit: "cover" }} unoptimized />
        {showPromo && (
          <span
            className="badge"
            style={{ position: "absolute", top: 10, left: 10, background: "#dc2626", color: "#fff" }}
          >
            -{p.discountPercent}%
          </span>
        )}
      </div>
      <div style={{ padding: "1rem" }}>
        <div style={{ fontWeight: 700, marginBottom: 4 }}>{p.name}</div>
        <div style={{ fontSize: "0.8rem", color: "var(--muted)", marginBottom: 8 }}>{p.shopName}</div>
        {p.averageRating != null && <RatingStars value={p.averageRating} />}
        <div style={{ marginTop: 8, display: "flex", alignItems: "baseline", gap: 8 }}>
          {showPromo ? (
            <>
              <span style={{ fontWeight: 800, fontSize: "1.1rem" }}>{Number(p.promoPrice).toFixed(2)} €</span>
              <span style={{ textDecoration: "line-through", color: "var(--muted)", fontSize: "0.9rem" }}>
                {Number(p.price).toFixed(2)} €
              </span>
            </>
          ) : (
            <span style={{ fontWeight: 800, fontSize: "1.1rem" }}>{Number(p.price).toFixed(2)} €</span>
          )}
        </div>
      </div>
    </Link>
  );
}
