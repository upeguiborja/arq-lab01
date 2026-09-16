import { useState, useEffect, useCallback } from 'react';
import type { Customer, Transaction } from './types';
import { api } from './api';
import { CustomersView } from './components/CustomersView';
import { TransactionsView } from './components/TransactionsView';
import { Toast } from './components/Toast';
import { Building2, Users, ArrowRightLeft, FileCode, AlertTriangle } from 'lucide-react';

interface ToastState {
  id: number;
  message: string;
  type: 'success' | 'error';
}

export function App() {
  const [activeTab, setActiveTab] = useState<'customers' | 'transactions'>('customers');
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [backendError, setBackendError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<ToastState[]>([]);

  const showToast = useCallback((message: string, type: 'success' | 'error') => {
    const id = Date.now();
    setToasts((prev) => [...prev, { id, message, type }]);
  }, []);

  const removeToast = useCallback((id: number) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setBackendError(null);
    try {
      const [custList, txList] = await Promise.all([
        api.getCustomers().catch((err) => {
          throw new Error(`Error al cargar clientes: ${err.message}`);
        }),
        api.getTransactions().catch((err) => {
          throw new Error(`Error al cargar transacciones: ${err.message}`);
        }),
      ]);
      setCustomers(custList || []);
      setTransactions(txList || []);
    } catch (err: any) {
      setBackendError(err.message || 'No se pudo conectar con el servidor Spring Boot (puerto 8080).');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return (
    <div className="app-container">
      {/* Header */}
      <header className="app-header">
        <div className="header-inner">
          <div className="brand-container">
            <div className="brand-icon">
              <Building2 size={24} />
            </div>
            <div>
              <h1 className="brand-title">Banco 2025</h1>
              <p className="brand-subtitle">Plataforma de Clientes y Transacciones</p>
            </div>
          </div>

          <div className="header-actions">
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noopener noreferrer"
              className="swagger-link"
              title="Abrir documentación interactiva de Swagger OpenAPI"
            >
              <FileCode size={16} />
              <span>Swagger Docs</span>
            </a>
          </div>
        </div>

        {/* Navigation Tabs */}
        <nav className="tab-nav">
          <button
            onClick={() => setActiveTab('customers')}
            className={`tab-btn ${activeTab === 'customers' ? 'tab-btn-active' : ''}`}
          >
            <Users size={18} />
            <span>Clientes ({customers.length})</span>
          </button>
          <button
            onClick={() => setActiveTab('transactions')}
            className={`tab-btn ${activeTab === 'transactions' ? 'tab-btn-active' : ''}`}
          >
            <ArrowRightLeft size={18} />
            <span>Transacciones ({transactions.length})</span>
          </button>
        </nav>
      </header>

      {/* Main Content Area */}
      <main className="app-main">
        {backendError && (
          <div className="backend-alert">
            <AlertTriangle size={20} className="shrink-0" />
            <div className="flex-1">
              <strong>Error de comunicación con el backend:</strong> {backendError}
              <div className="mt-1 text-xs opacity-90">
                Asegúrate de que la aplicación Spring Boot esté corriendo en el puerto 8080.
              </div>
            </div>
            <button onClick={fetchData} className="alert-retry-btn">
              Reintentar
            </button>
          </div>
        )}

        {loading && !backendError ? (
          <div className="loading-state">
            <div className="loading-spinner"></div>
            <p>Cargando información del banco...</p>
          </div>
        ) : (
          <>
            {activeTab === 'customers' && (
              <CustomersView
                customers={customers}
                onRefresh={fetchData}
                onShowToast={showToast}
              />
            )}
            {activeTab === 'transactions' && (
              <TransactionsView
                transactions={transactions}
                customers={customers}
                onRefresh={fetchData}
                onShowToast={showToast}
              />
            )}
          </>
        )}
      </main>

      {/* Toast notifications container */}
      <div className="toast-container">
        {toasts.map((toast) => (
          <Toast
            key={toast.id}
            message={toast.message}
            type={toast.type}
            onClose={() => removeToast(toast.id)}
          />
        ))}
      </div>
    </div>
  );
}
export default App;
