export interface Customer {
  id?: number;
  firstName: string;
  lastName: string;
  accountNumber: string;
  balance: number;
}

export interface Transaction {
  id?: number;
  senderAccountNumber: string;
  receiverAccountNumber: string;
  amount: number;
  timestamp?: string;
}

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}
