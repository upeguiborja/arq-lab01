import React, { useState } from 'react';
import type { Customer } from '../types';
import { api, ApiError } from '../api';
import { Plus, Edit2, Trash2, Search, RefreshCw, UserCheck, DollarSign, AlertCircle } from 'lucide-react';

interface CustomersViewProps {
  customers: Customer[];
  onRefresh: () => Promise<void>;
  onShowToast: (message: string, type: 'success' | 'error') => void;
}

export const CustomersView: React.FC<CustomersViewProps> = ({ customers, onRefresh, onShowToast }) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | null>(null);
  const [customerToDelete, setCustomerToDelete] = useState<Customer | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  // Form states
  const [formData, setFormData] = useState<Customer>({
    accountNumber: '',
    firstName: '',
    lastName: '',
    balance: 0,
  });

  const filteredCustomers = customers.filter(
    (c) =>
      c.accountNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.firstName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      c.lastName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalBalance = customers.reduce((sum, c) => sum + (Number(c.balance) || 0), 0);

  const openCreateModal = () => {
    setFormData({
      accountNumber: '',
      firstName: '',
      lastName: '',
      balance: 1000,
    });
    setFieldErrors({});
    setIsCreateModalOpen(true);
  };

  const openEditModal = (customer: Customer) => {
    setEditingCustomer(customer);
    setFormData({
      accountNumber: customer.accountNumber,
      firstName: customer.firstName,
      lastName: customer.lastName,
      balance: customer.balance,
    });
    setFieldErrors({});
  };

  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
    setFieldErrors({});

    try {
      await api.createCustomer(formData);
      onShowToast(`Cliente ${formData.firstName} creado exitosamente`, 'success');
      setIsCreateModalOpen(false);
      await onRefresh();
    } catch (err: any) {
      if (err instanceof ApiError && err.problem?.errors) {
        setFieldErrors(err.problem.errors);
      }
      onShowToast(err.message || 'Error al crear cliente', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleUpdateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCustomer?.id) return;
    setIsSubmitting(true);
    setFieldErrors({});

    try {
      await api.updateCustomer(editingCustomer.id, formData);
      onShowToast(`Cliente ${formData.firstName} actualizado exitosamente`, 'success');
      setEditingCustomer(null);
      await onRefresh();
    } catch (err: any) {
      if (err instanceof ApiError && err.problem?.errors) {
        setFieldErrors(err.problem.errors);
      }
      onShowToast(err.message || 'Error al actualizar cliente', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteConfirm = async () => {
    if (!customerToDelete?.id) return;
    setIsSubmitting(true);
    try {
      await api.deleteCustomer(customerToDelete.id);
      onShowToast(`Cliente #${customerToDelete.id} eliminado correctamente`, 'success');
      setCustomerToDelete(null);
      await onRefresh();
    } catch (err: any) {
      onShowToast(err.message || 'Error al eliminar cliente', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="section-container">
      {/* Stats row */}
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon bg-blue-100 text-blue-600">
            <UserCheck size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Total Clientes</span>
            <span className="stat-value">{customers.length}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon bg-emerald-100 text-emerald-600">
            <DollarSign size={24} />
          </div>
          <div className="stat-info">
            <span className="stat-label">Depósitos Totales</span>
            <span className="stat-value">
              ${totalBalance.toLocaleString('es-CO', { minimumFractionDigits: 2 })}
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
            placeholder="Buscar por nombre o número de cuenta..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
        </div>

        <div className="action-buttons">
          <button onClick={onRefresh} className="btn btn-secondary" title="Recargar lista">
            <RefreshCw size={18} />
            <span>Actualizar</span>
          </button>
          <button onClick={openCreateModal} className="btn btn-primary">
            <Plus size={18} />
            <span>Nuevo Cliente</span>
          </button>
        </div>
      </div>

      {/* Customer Table */}
      <div className="table-card">
        {filteredCustomers.length === 0 ? (
          <div className="empty-state">
            <UserCheck size={48} className="empty-icon" />
            <p className="empty-text">No se encontraron clientes.</p>
            {searchTerm && <p className="empty-subtext">Intenta ajustar tu búsqueda.</p>}
          </div>
        ) : (
          <div className="table-responsive">
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre Completo</th>
                  <th>No. de Cuenta</th>
                  <th className="text-right">Saldo</th>
                  <th className="text-center">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {filteredCustomers.map((customer) => (
                  <tr key={customer.id}>
                    <td className="font-mono text-muted">#{customer.id}</td>
                    <td className="font-medium">
                      {customer.firstName} {customer.lastName}
                    </td>
                    <td>
                      <span className="badge badge-account font-mono">{customer.accountNumber}</span>
                    </td>
                    <td className="text-right font-mono font-semibold">
                      ${Number(customer.balance).toLocaleString('es-CO', { minimumFractionDigits: 2 })}
                    </td>
                    <td>
                      <div className="actions-cell">
                        <button
                          onClick={() => openEditModal(customer)}
                          className="action-btn edit-btn"
                          title="Editar cliente"
                        >
                          <Edit2 size={16} />
                        </button>
                        <button
                          onClick={() => setCustomerToDelete(customer)}
                          className="action-btn delete-btn"
                          title="Eliminar cliente"
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create Modal */}
      {isCreateModalOpen && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Crear Nuevo Cliente</h3>
              <button onClick={() => setIsCreateModalOpen(false)} className="close-btn">
                &times;
              </button>
            </div>
            <form onSubmit={handleCreateSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Número de Cuenta *</label>
                  <input
                    type="text"
                    required
                    value={formData.accountNumber}
                    onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                    placeholder="Ej. 123456789"
                    className={`form-input ${fieldErrors.accountNumber ? 'input-error' : ''}`}
                  />
                  {fieldErrors.accountNumber && (
                    <span className="field-error-msg">{fieldErrors.accountNumber}</span>
                  )}
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Nombre *</label>
                    <input
                      type="text"
                      required
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      placeholder="Ej. Mateo"
                      className={`form-input ${fieldErrors.firstName ? 'input-error' : ''}`}
                    />
                    {fieldErrors.firstName && (
                      <span className="field-error-msg">{fieldErrors.firstName}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>Apellido *</label>
                    <input
                      type="text"
                      required
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      placeholder="Ej. Upegui"
                      className={`form-input ${fieldErrors.lastName ? 'input-error' : ''}`}
                    />
                    {fieldErrors.lastName && (
                      <span className="field-error-msg">{fieldErrors.lastName}</span>
                    )}
                  </div>
                </div>

                <div className="form-group">
                  <label>Saldo Inicial *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={formData.balance}
                    onChange={(e) => setFormData({ ...formData, balance: parseFloat(e.target.value) || 0 })}
                    placeholder="100000.00"
                    className={`form-input ${fieldErrors.balance ? 'input-error' : ''}`}
                  />
                  {fieldErrors.balance && (
                    <span className="field-error-msg">{fieldErrors.balance}</span>
                  )}
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  onClick={() => setIsCreateModalOpen(false)}
                  className="btn btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" disabled={isSubmitting} className="btn btn-primary">
                  {isSubmitting ? 'Guardando...' : 'Crear Cliente'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {editingCustomer && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Editar Cliente #{editingCustomer.id}</h3>
              <button onClick={() => setEditingCustomer(null)} className="close-btn">
                &times;
              </button>
            </div>
            <form onSubmit={handleUpdateSubmit}>
              <div className="modal-body">
                <div className="form-group">
                  <label>Número de Cuenta *</label>
                  <input
                    type="text"
                    required
                    value={formData.accountNumber}
                    onChange={(e) => setFormData({ ...formData, accountNumber: e.target.value })}
                    className={`form-input ${fieldErrors.accountNumber ? 'input-error' : ''}`}
                  />
                  {fieldErrors.accountNumber && (
                    <span className="field-error-msg">{fieldErrors.accountNumber}</span>
                  )}
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label>Nombre *</label>
                    <input
                      type="text"
                      required
                      value={formData.firstName}
                      onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
                      className={`form-input ${fieldErrors.firstName ? 'input-error' : ''}`}
                    />
                    {fieldErrors.firstName && (
                      <span className="field-error-msg">{fieldErrors.firstName}</span>
                    )}
                  </div>

                  <div className="form-group">
                    <label>Apellido *</label>
                    <input
                      type="text"
                      required
                      value={formData.lastName}
                      onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
                      className={`form-input ${fieldErrors.lastName ? 'input-error' : ''}`}
                    />
                    {fieldErrors.lastName && (
                      <span className="field-error-msg">{fieldErrors.lastName}</span>
                    )}
                  </div>
                </div>

                <div className="form-group">
                  <label>Saldo *</label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    required
                    value={formData.balance}
                    onChange={(e) => setFormData({ ...formData, balance: parseFloat(e.target.value) || 0 })}
                    className={`form-input ${fieldErrors.balance ? 'input-error' : ''}`}
                  />
                  {fieldErrors.balance && (
                    <span className="field-error-msg">{fieldErrors.balance}</span>
                  )}
                </div>
              </div>

              <div className="modal-footer">
                <button
                  type="button"
                  onClick={() => setEditingCustomer(null)}
                  className="btn btn-secondary"
                >
                  Cancelar
                </button>
                <button type="submit" disabled={isSubmitting} className="btn btn-primary">
                  {isSubmitting ? 'Guardando...' : 'Guardar Cambios'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Delete Confirmation Modal */}
      {customerToDelete && (
        <div className="modal-backdrop">
          <div className="modal-content modal-sm">
            <div className="modal-header">
              <div className="flex items-center gap-2 text-rose-600">
                <AlertCircle size={20} />
                <h3>Confirmar Eliminación</h3>
              </div>
              <button onClick={() => setCustomerToDelete(null)} className="close-btn">
                &times;
              </button>
            </div>
            <div className="modal-body">
              <p>
                ¿Estás seguro de que deseas eliminar al cliente{' '}
                <strong>
                  {customerToDelete.firstName} {customerToDelete.lastName}
                </strong>{' '}
                (Cuenta: <code>{customerToDelete.accountNumber}</code>)?
              </p>
              <p className="text-sm text-rose-600 mt-2">Esta acción no se puede deshacer.</p>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                onClick={() => setCustomerToDelete(null)}
                className="btn btn-secondary"
              >
                Cancelar
              </button>
              <button
                type="button"
                onClick={handleDeleteConfirm}
                disabled={isSubmitting}
                className="btn btn-danger"
              >
                {isSubmitting ? 'Eliminando...' : 'Eliminar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
