import type { InputHTMLAttributes } from "react";

type Props = InputHTMLAttributes<HTMLInputElement> & {
    label: string;
    error?: string;
};

export function TextField({ label, error, className = "", ...props }: Props) {
    return (
        <div className="space-y-1">
            <label className="block text-sm font-medium text-gray-900">{label}</label>
            <input
                className={[
                    "w-full rounded-lg border bg-white px-3 py-2 text-sm",
                    error ? "border-red-300 focus:outline-none focus:ring-2 focus:ring-red-200" : "border-gray-300",
                    className,
                ].join(" ")}
                {...props}
            />
            {error && <div className="text-sm text-red-700">{error}</div>}
        </div>
    );
}
