export function Alert({
  title,
  message,
  tone = "warning",
}: {
  title: string;
  message: string;
  tone?: "warning" | "error" | "info";
}) {
  const styles =
    tone === "error"
      ? "border-red-300 bg-red-50 text-red-900"
      : tone === "info"
        ? "border-blue-300 bg-blue-50 text-blue-900"
        : "border-amber-300 bg-amber-50 text-amber-900";

  return (
    <div className={`rounded-lg border p-3 text-sm ${styles}`}>
      <div className="font-semibold">{title}</div>
      <div className="break-words">{message}</div>
    </div>
  );
}
