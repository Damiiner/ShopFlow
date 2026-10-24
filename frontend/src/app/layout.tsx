import type { Metadata } from "next";
import { AuthProvider } from "@/lib/auth";
import { Header } from "@/components/Header";
import "./globals.css";

export const metadata: Metadata = {
  title: "ShopFlow",
  description: "Boutique en ligne — Mini-projet",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr">
      <body>
        <AuthProvider>
          <Header />
          <main>{children}</main>
          <footer style={{ marginTop: "4rem", padding: "2rem", textAlign: "center", color: "var(--muted)", fontSize: "0.875rem" }}>
            ShopFlow
          </footer>
        </AuthProvider>
      </body>
    </html>
  );
}
