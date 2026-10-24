const colors: Record<string, { bg: string; fg: string }> = {
  PENDING: { bg: "#fef3c7", fg: "#92400e" },
  PAID: { bg: "#dbeafe", fg: "#1e40af" },
  PROCESSING: { bg: "#e0e7ff", fg: "#3730a3" },
  SHIPPED: { bg: "#cffafe", fg: "#0e7490" },
  DELIVERED: { bg: "#d1fae5", fg: "#065f46" },
  CANCELLED: { bg: "#fee2e2", fg: "#991b1b" },
};

export function OrderStatusBadge({ status }: { status: string }) {
  const c = colors[status] ?? { bg: "#f3f4f6", fg: "#374151" };
  return (
    <span
      style={{
        display: "inline-block",
        padding: "0.25rem 0.6rem",
        borderRadius: 999,
        fontSize: "0.75rem",
        fontWeight: 700,
        background: c.bg,
        color: c.fg,
      }}
    >
      {status}
    </span>
  );
}
