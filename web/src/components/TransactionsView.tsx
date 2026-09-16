import React, { useState } from 'react';
import type { Customer, Transaction } from '../types';
import { api, ApiError } from '../api';
import { ArrowRightLeft, Plus, Search, RefreshCw, Send, DollarSign, Calendar, ArrowRight } from 'lucide-react';

interface TransactionsViewProps {
  transactions: Transaction[];
  customers: Customer[];
  onRefresh: () => Promise<void>;
  onShowToast: (message: string, type: 'success' | 'error') => void;
}

export const TransactionsView: React.FC<TransactionsViewProps> = ({
  transactions,
  customers,
  onRefresh,
  onShowToast,
}) => {
  const [filterAccount, setFilterAccount] = useState('');
  const [isTransferModalOpen, setIsTransferModalOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [transferData, setTransferData] = useState<Transaction>({
    senderAccountNumber: '',
    receiverAccountNumber: '',
    amount: 100,
  });

  const filteredTransactions = transactions.filter((t) => {
    if (!filterAccount.trim()) return true;
    const query = filterAccount.toLowerCase();
    return (
      t.senderAccountNumber?.toLowerCase().includes(query) ||
      t.receiverAccountNumber?.toLowerCase().includes(query)
    );
  });

  const totalTransferred = transactions.reduce((sum, t) => sum + (Number(t.amount) || 0), 0);

  const selectedSender = customers.find((c) => c.accountNumber === transferData.senderAccountNumber);

  const openTransferModal = () => {
    setTransferData({
      senderAccountNumber: customers[0]?.accountNumber || '',
      receiverAccountNumber: customers[1]?.accountNumber || '',
      amount: 100,
    });
    setFieldErrors({});
    setIsTransferModalOpen(true);
  };

  const handleTransferSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFieldErrors({});

    if (!transferData.senderAccountNumber) {
      setFieldErrors((prev) => ({ ...prev, senderAccountNumber: 'Selecciona una cuenta de origen' }));
      return;
    }
    if (!transferData.receiverAccountNumber) {
      setFieldErrors((prev) => ({ ...prev, receiverAccountNumber: 'Selecciona una cuenta de destino' }));
      return;
    }
    if (transferData.senderAccountNumber === transferData.receiverAccountNumber) {
      setFieldErrors((prev) => ({
        ...prev,
        receiverAccountNumber: 'La cuenta de origen y destino no pueden ser iguales',
      }));
      return;
    }
    if (transferData.amount <= 0) {
      setFieldErrors((prev) => ({ ...prev, amount: 'El monto debe ser mayor a cero' }));
      return;
    }
    if (selectedSender && selectedSender.balance < transferData.amount) {
      setFieldErrors((prev) => ({ ...prev, amount: 'Saldo insuficiente en la cuenta de origen' }));
      return;
    }

    setIsSubmitting(true);
    try {
      await api.createTransaction(transferData);
      onShowToast(
        `Transferencia de $${transferData.amount.toLocaleString()} realizada exitosamente`,
        'success'
      );
      setIsTransferModalOpen(false);
      await onRefresh();
    } catch (err: any) {
      if (err instanceof ApiError && err.problem?.errors) {
        setFieldErrors(err.problem.errors);
      }
      onShowToast(err.message || 'Error al procesar transferencia', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (isoString?: string) => {
    if (!isoString) return '-';
    try {
      const date = new Date(isoString);
      return date.toLocaleString('es-CO', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    } catch {
      return isoString;
    }
  };

  return (
    <div className="section-container">
      {/* Stats row */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon bg-purple-100 text-purple-600">
            <ArrowRightLeft size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Total Transacciones</span>
            <span className="stat-value">{transactions.length}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon bg-indigo-100 text-indigo-600">
            <DollarSign size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Volumen Total Transferido</span>
            <span className="stat-value">
              ${totalTransferred.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
            </span>
          </div>
        </div>
      </div>

      {/* Action Bar */}
      <div className="action-bar">
        <div className="search-box">
          <Search size={18} className="search-icon" />
          <input
            type="text"
            placeholder="Filtrar por número de cuenta remitente o destinatario..."
            value={filterAccount}
            onChange={(e) => setFilterAccount(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="action-buttons">
          <button onClick={onRefresh} className="btn btn-secondary" title="Recargar transacciones">
            <RefreshCw size={18} />
            <span>Actualizar</span>
          </button>
          <button
            onClick={openTransferModal}
            className="btn btn-primary"
            disabled={customers.length < 2}
            title={customers.length < 2 ? 'Se necesitan al menos 2 clientes para transferir' : ''}
          >
            <Plus size={18} />
            <span>Nueva Transferencia</span>
          </button>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="table-card">
        {filteredTransactions.length === 0 ? (
          <div className="empty-state">
            <ArrowRightLeft size={48} className="empty-icon" />
            <p className="empty-text">No hay transacciones registradas.</p>
            {filterAccount && <p className="empty-subtext">Intenta ajustar el filtro de cuenta.</p>}
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Fecha y Hora</th>
                  <th>Cuenta Origen</th>
                  <th className="text-center">Flujo</th>
                  <th>Cuenta Destino</th>
                  <th className="text-right">Monto</th>
                  <th className="text-center">Estado</th>
                </tr>
              </thead>
              <tbody>
                {filteredTransactions.map((tx) => (
                  <tr key={tx.id}>
                    <td className="font-mono text-muted">#{tx.id}</td>
                    <td className="text-sm">
                      <span className="flex items-center gap-1.5 text-slate-600">
                        <Calendar size={14} />
                        {formatDate(tx.timestamp)}
                      </span>
                    </td>
                    <td>
                      <span className="badge badge-sender font-mono">{tx.senderAccountNumber}</span>
                    </td>
                    <td className="text-center text-slate-400">
                      <ArrowRight size={16} className="inline" />
                    </td>
                    <td>
                      <span className="badge badge-receiver font-mono">{tx.receiverAccountNumber}</span>
                    </td>
                    <td className="text-right font-mono font-bold text-emerald-600">
                      ${Number(tx.amount).toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="text-center">
                      <span className="badge badge-success">Completada</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Transfer Modal */}
      {isTransferModalOpen && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <div className="flex items-center gap-2">
                <Send size={20} className="text-indigo-600" />
                <h3>Transferir Dinero</h3>
              </div>
              <button onClick={() => setIsTransferModalOpen(false)} className="close-btn">
                &times;
              </button>
            </div>
            <form onSubmit={handleTransferSubmit}>
              <div className="modal-body">
                {/* Sender Account */}
                <div className="form-group">
                  <label>Cuenta Remitente (Origen) *</label>
                  <select
                    value={transferData.senderAccountNumber}
                    onChange={(e) =>
                      setTransferData({ ...transferData, senderAccountNumber: e.target.value })
                    }
                    className={`form-input ${fieldErrors.senderAccountNumber ? 'input-error' : ''}`}
                    required
                  >
                    <option value="">-- Seleccionar cuenta origen --</option>
                    {customers.map((c) => (
                      <option key={c.id} value={c.accountNumber}>
                        {c.firstName} {c.lastName} ({c.accountNumber}) - Saldo: $
                        {Number(c.balance).toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                      </option>
                    ))}
                  </select>
                  {fieldErrors.senderAccountNumber && (
                    <span className="field-error-msg">{fieldErrors.senderAccountNumber}</span>
                  )}
                  {selectedSender && (
                    <div className="account-balance-preview">
                      Saldo disponible:{' '}
                      <strong>
                        ${Number(selectedSender.balance).toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                      </strong>
                    </div>
                  )}
                </div>

                {/* Receiver Account */}
                <div className="form-group">
                  <label>Cuenta Destinatario (Destino) *</label>
                  <select
                    value={transferData.receiverAccountNumber}
                    onChange={(e) =>
                      setTransferData({ ...transferData, receiverAccountNumber: e.target.value })
                    }
                    className={`form-input ${fieldErrors.receiverAccountNumber ? 'input-error' : ''}`}
                    required
                  >
                    <option value="">-- Seleccionar cuenta destino --</option>
                    {customers
                      .filter((c) => c.accountNumber !== transferData.senderAccountNumber)
                      .map((c) => (
                        <option key={c.id} value={c.accountNumber}>
                          {c.firstName} {c.lastName} ({c.accountNumber})
                        </option>
                      ))}
                  </select>
                  {fieldErrors.receiverAccountNumber && (
                    <span className="field-error-msg">{fieldErrors.receiverAccountNumber}</span>
                  )}
                </div>

                {/* Amount */}
                <div className="form-group">
                  <label>Monto a Transferir *</label>
                  <div className="amount-input-wrapper">
                    <span className="currency-symbol">$</span>
                    <input
                      type="number"
                      step="0.01"
                      min="0.01"
                      required
                      value={transferData.amount}
                      onChange={(e) =>
                        setTransferData({ ...transferData, amount: parseFloat(e.target.value) || 0 })
                      }
                      placeholder="0.00"
                      className={`form-input pl-8 ${fieldErrors.amount ? 'input-error' : ''}`}
                    />
                  </div>
                  {fieldErrors.amount && <span className="field-error-msg">{fieldErrors.amount}</span>}

                  {/* Quick amounts */}
                  <div className="quick-amounts">
                    {[50, 100, 500, 1000].map((amt) => (
                      <button
                        key={amt}
                        type="button"
                        onClick={() => setTransferData({ ...transferData, amount: amt })}
                        className="quick-amt-btn"
                      >
                        +${amt}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  onClick={() => setIsTransferModalOpen(false)}
                  className="btn btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" disabled={isSubmitting} className="btn btn-primary">
                  <Send size={16} />
                  <span>{isSubmitting ? 'Transfiriendo...' : 'Confirmar Transferencia'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
