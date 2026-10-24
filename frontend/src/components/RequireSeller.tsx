"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { useAuth } from "@/lib/auth";

export function RequireSeller({ children }: { children: React.ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && (!user || user.role !== "SELLER")) {
      router.replace("/login");
    }
  }, [user, loading, router]);

  if (loading || !user || user.role !== "SELLER") {
    return (
      <div className="container" style={{ padding: "3rem 0" }}>
        <div className="skeleton" style={{ height: 200 }} />
      </div>
    );
  }

  return <>{children}</>;
}
