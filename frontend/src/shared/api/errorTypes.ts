export type FieldError = {
    field: string;
    message: string;
};

export type ApiErrorBody = {
    timestamp?: string;
    status: number;
    error?: string;
    message?: string;
    path?: string;
    fieldErrors?: FieldError[];
};
