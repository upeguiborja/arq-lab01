import type { Customer, Transaction, ProblemDetail } from './types';

export class ApiError extends Error {
  status: number;
  problem?: ProblemDetail;

  constructor(message: string, status: number, problem?: ProblemDetail) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.problem = problem;
  }
}

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...options?.headers,
    },
  });

  if (response.status === 204) {
    return {} as T;
  }

  const text = await response.text();
  let data: any;
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = { detail: text };
  }

  if (!response.ok) {
    let errorMessage = data.detail || data.message || `Error ${response.status}: ${response.statusText}`;
    if (data.errors && typeof data.errors === 'object') {
      const fieldErrors = Object.entries(data.errors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ');
      errorMessage = `${errorMessage} (${fieldErrors})`;
    }
    throw new ApiError(errorMessage, response.status, data);
  }

  return data as T;
}

export const api = {
  // Clientes
  getCustomers: () => request<Customer[]>('/api/customers'),
  getCustomerById: (id: number) => request<Customer>(`/api/customers/${id}`),
  createCustomer: (customer: Customer) =>
    request<Customer>('/api/customers', {
      method: 'POST',
      body: JSON.stringify(customer),
    }),
  updateCustomer: (id: number, customer: Customer) =>
    request<Customer>(`/api/customers/${id}`, {
      method: 'PUT',
      body: JSON.stringify(customer),
    }),
  deleteCustomer: (id: number) =>
    request<void>(`/api/customers/${id}`, {
      method: 'DELETE',
    }),

  // Transacciones
  getTransactions: () => request<Transaction[]>('/api/transactions'),
  getTransactionById: (id: number) => request<Transaction>(`/api/transactions/${id}`),
  createTransaction: (transaction: Transaction) =>
    request<Transaction>('/api/transactions', {
      method: 'POST',
      body: JSON.stringify(transaction),
    }),
};
