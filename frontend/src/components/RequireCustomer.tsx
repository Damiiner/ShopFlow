"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "@/lib/auth";

export function RequireCustomer({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && (!user || user.role !== "CUSTOMER")) {
      router.replace("/login?next=" + encodeURIComponent(window.location.pathname));
    }
  }, [user, loading, router]);

  if (loading || !user || user.role !== "CUSTOMER") {
    return (
      <div className="container" style={{ padding: "3rem 0" }}>
        <div className="skeleton" style={{ height: 200 }} />
      </div>
    );
  }

  return <>{children}</>;
}
