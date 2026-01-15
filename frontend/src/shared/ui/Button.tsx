import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "danger";
  fullWidth?: boolean;
};

export function Button({
  variant = "secondary",
  fullWidth = false,
  className = "",
  ...props
}: Props) {
  const base =
    "inline-flex items-center justify-center rounded-lg px-3 py-2 text-sm font-semibold transition focus:outline-none focus:ring-2 focus:ring-offset-2";
  const width = fullWidth ? "w-full" : "";
  const styles =
    variant === "primary"
      ? "bg-gray-900 text-white hover:bg-gray-800 focus:ring-gray-900"
      : variant === "danger"
        ? "bg-red-600 text-white hover:bg-red-500 focus:ring-red-600"
        : "border bg-white text-gray-900 hover:bg-gray-50 focus:ring-gray-300";

  return <button className={`${base} ${styles} ${width} ${className}`} {...props} />;
}
