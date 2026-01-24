import React from "react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
    children: React.ReactNode;
    fullWidth?: boolean;
}

export function Button({ children, fullWidth, className = "", ...props }: ButtonProps) {
    return (
        <button
            className={`bg-blue-600 text-white py-2 px-4 rounded font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed ${fullWidth ? "w-full" : ""} ${className}`}
            {...props}
        >
            {children}
        </button>
    );
}
