export function RatingStars({ value }: { value: number }) {
  const full = Math.round(value);
  return (
    <span style={{ color: "#f59e0b", fontSize: "0.85rem", letterSpacing: 1 }} aria-label={`${value} sur 5`}>
      {"★".repeat(full)}
      {"☆".repeat(5 - full)}
      <span style={{ color: "var(--muted)", marginLeft: 4 }}>{value.toFixed(1)}</span>
    </span>
  );
}
