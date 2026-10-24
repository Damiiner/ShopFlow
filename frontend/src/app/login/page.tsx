"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { Suspense, useState } from "react";
import { useAuth } from "@/lib/auth";

function LoginForm() {
  const { login } = useAuth();
  const router = useRouter();
  const sp = useSearchParams();
  const next = sp.get("next") ?? "/";
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState<string | null>(null);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErr(null);
    try {
      await login(email, password);
      router.push(next);
    } catch {
      setErr("Email ou mot de passe incorrect");
    }
  };

  return (
    <div className="container" style={{ maxWidth: 400, padding: "3rem 0" }}>
      <h1>Connexion</h1>
      <p style={{ color: "var(--muted)" }}>
        Démo : <code>customer@shopflow.com</code> / <code>Customer123</code>
      </p>
      <form onSubmit={submit} className="card" style={{ padding: "1.5rem", display: "flex", flexDirection: "column", gap: "1rem" }}>
        <div>
          <label className="label">Email</label>
          <input className="input" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </div>
        <div>
          <label className="label">Mot de passe</label>
          <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        </div>
        {err && <p style={{ color: "#b91c1c", margin: 0 }}>{err}</p>}
        <button type="submit" className="btn btn-primary">
          Se connecter
        </button>
      </form>
      <p style={{ marginTop: "1rem" }}>
        Pas de compte ? <Link href="/register">Inscription</Link>
      </p>
    </div>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<div className="container skeleton" style={{ height: 300, margin: "2rem auto" }} />}>
      <LoginForm />
    </Suspense>
  );
}
