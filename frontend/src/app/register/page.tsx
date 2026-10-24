"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/lib/auth";

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [role, setRole] = useState<"CUSTOMER" | "SELLER">("CUSTOMER");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [shopName, setShopName] = useState("");
  const [shopDescription, setShopDescription] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    if (password !== confirmPassword) {
      setErr("Les mots de passe ne correspondent pas.");
      return;
    }
    const body: Record<string, unknown> = {
      email,
      password,
      firstName,
      lastName,
      role,
    };
    if (role === "SELLER") {
      body.shopName = shopName;
      body.shopDescription = shopDescription;
    }
    try {
      await register(body);
      router.push(role === "SELLER" ? "/seller" : "/");
    } catch (e) {
      setErr(String((e as Error).message));
    }
  };

  return (
    <div className="container" style={{ maxWidth: 440, padding: "3rem 0" }}>
      <h1>Inscription</h1>
      <p style={{ color: "var(--muted)", fontSize: "0.9rem" }}>
        Mot de passe : au moins 8 caractères, une majuscule, une minuscule, un chiffre.
      </p>
      <form onSubmit={submit} className="card" style={{ padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1rem" }}>
        <div>
          <label className="label">Prénom</label>
          <input className="input" value={firstName} onChange={(e) => setFirstName(e.target.value)} required />
        </div>
        <div>
          <label className="label">Nom</label>
          <input className="input" value={lastName} onChange={(e) => setLastName(e.target.value)} required />
        </div>
        <div>
          <label className="label">Email</label>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            pattern="^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"
            required
          />
        </div>
        <div>
          <label className="label">Mot de passe</label>
          <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        <div>
          <label className="label">Retaper le mot de passe</label>
          <input
            className="input"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="label">Type de compte</label>
          <select className="input" value={role} onChange={(e) => setRole(e.target.value as "CUSTOMER" | "SELLER")}>
            <option value="CUSTOMER">Client</option>
            <option value="SELLER">Vendeur</option>
          </select>
        </div>
        {role === "SELLER" && (
          <>
            <div>
              <label className="label">Nom de la boutique</label>
              <input className="input" value={shopName} onChange={(e) => setShopName(e.target.value)} required />
            </div>
            <div>
              <label className="label">Description</label>
              <textarea className="input" rows={3} value={shopDescription} onChange={(e) => setShopDescription(e.target.value)} />
            </div>
          </>
        )}
        {err && <p style={{ color: "#b91c1c", margin: 0, fontSize: "0.9rem" }}>{err}</p>}
        <button type="submit" className="btn btn-primary">
          Créer mon compte
        </button>
      </form>
      <p style={{ marginTop: "1rem" }}>
        Déjà inscrit ? <Link href="/login">Connexion</Link>
      </p>
    </div>
  );
}
