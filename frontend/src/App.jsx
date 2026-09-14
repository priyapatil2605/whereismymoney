import { useEffect, useState } from "react";
import {
  BrowserRouter,
  Routes,
  Route,
  Navigate,
  Link,
  useNavigate,
} from "react-router-dom";

const API_URL = "http://localhost:8080";

async function apiRequest(path, options = {}) {
  const token = localStorage.getItem("token");

  const headers = {
    ...(options.headers || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  const text = await response.text();

  let data;
  try {
    data = text ? JSON.parse(text) : {};
  } catch {
    data = text;
  }

  if (!response.ok) {
    throw new Error(
      data?.message ||
      data?.error ||
      "Request failed"
    );
  }

  return data;
}

/* =========================
   PROTECTED ROUTE
========================= */

function ProtectedRoute({ children }) {
  return localStorage.getItem("token")
    ? children
    : <Navigate to="/login" replace />;
}

/* =========================
   LANDING PAGE
========================= */

function Landing() {
  const navigate = useNavigate();

  return (
    <div className="landing">
      <nav className="landing-nav">
        <div className="brand">
          WHERE<span>IS</span>MYMONEY
        </div>

        <div className="nav-actions">
          <button
            className="btn btn-secondary"
            onClick={() => navigate("/login")}
          >
            Login
          </button>

          <button
            className="btn btn-primary"
            onClick={() => navigate("/register")}
          >
            Get Started
          </button>
        </div>
      </nav>

      <main className="hero">
        <div className="hero-badge">
          FINANCIAL INTELLIGENCE PLATFORM
        </div>

        <h1>
          Understand your money.
          <br />
          <span>Understand your investments.</span>
        </h1>

        <p>
          Track transactions, analyze portfolios, measure risk,
          import financial data and ask AI-powered questions about
          your finances.
        </p>

        <div className="currency-animation">
          ₹
          <span>$</span>
          <span>€</span>
          <span>£</span>
          <span>¥</span>
          <span>₩</span>
        </div>

        <div className="hero-buttons">
          <button
            className="btn btn-primary btn-large"
            onClick={() => navigate("/register")}
          >
            Start Analyzing
          </button>

          <button
            className="btn btn-secondary btn-large"
            onClick={() => navigate("/login")}
          >
            Sign In
          </button>
        </div>
      </main>

      <div className="feature-strip">
        <div>
          <strong>TRANSACTIONS</strong>
          <span>Organize your financial activity</span>
        </div>

        <div>
          <strong>RISK ANALYTICS</strong>
          <span>Volatility · VaR · Drawdown</span>
        </div>

        <div>
          <strong>AI ANALYST</strong>
          <span>Ask questions about your portfolio</span>
        </div>

        <div>
          <strong>IMPORT</strong>
          <span>CSV · Excel · PDF</span>
        </div>
      </div>
    </div>
  );
}

/* =========================
   LOGIN
========================= */

function Login() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleLogin(e) {
    e.preventDefault();

    setLoading(true);
    setError("");

    try {
      const data = await apiRequest("/api/auth/login", {
        method: "POST",
        body: JSON.stringify({
          email,
          password,
        }),
      });

      localStorage.setItem("token", data.token);
      localStorage.setItem("userId", data.userId);
      localStorage.setItem("email", data.email);

      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout title="Welcome back">
      <form onSubmit={handleLogin} className="auth-form">

        <label>Email</label>

        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />

        <label>Password</label>

        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="••••••••"
          required
        />

        {error && <div className="error-box">{error}</div>}

        <button
          className="btn btn-primary full"
          disabled={loading}
        >
          {loading ? "Signing in..." : "Sign In"}
        </button>

        <p className="auth-switch">
          Don't have an account?{" "}
          <Link to="/register">Create one</Link>
        </p>
      </form>
    </AuthLayout>
  );
}

/* =========================
   REGISTER
========================= */

function Register() {
  const navigate = useNavigate();

  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleRegister(e) {
    e.preventDefault();

    setLoading(true);
    setError("");

    try {
      const data = await apiRequest("/api/auth/signup", {
        method: "POST",
        body: JSON.stringify({
          fullName,
          email,
          password,
        }),
      });

      localStorage.setItem("token", data.token);
      localStorage.setItem("userId", data.userId);
      localStorage.setItem("email", data.email);

      navigate("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <AuthLayout title="Create your account">
      <form onSubmit={handleRegister} className="auth-form">

        <label>Full Name</label>

        <input
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          placeholder="Your name"
          required
        />

        <label>Email</label>

        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="you@example.com"
          required
        />

        <label>Password</label>

        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Create a password"
          required
        />

        {error && <div className="error-box">{error}</div>}

        <button
          className="btn btn-primary full"
          disabled={loading}
        >
          {loading ? "Creating..." : "Create Account"}
        </button>

        <p className="auth-switch">
          Already have an account?{" "}
          <Link to="/login">Sign in</Link>
        </p>
      </form>
    </AuthLayout>
  );
}

function AuthLayout({ title, children }) {
  return (
    <div className="auth-page">
      <div className="auth-card">

        <Link to="/" className="auth-brand">
          WHERE<span>IS</span>MYMONEY
        </Link>

        <h1>{title}</h1>

        {children}
      </div>
    </div>
  );
}

/* =========================
   MAIN LAYOUT
========================= */

function Layout({ children }) {
  const navigate = useNavigate();

  function logout() {
    localStorage.clear();
    navigate("/login");
  }

  return (
    <div className="app-shell">

      <aside className="sidebar">

        <div className="sidebar-brand">
          WHERE<span>IS</span>MYMONEY
        </div>

        <div className="sidebar-subtitle">
          Financial Intelligence
        </div>

        <nav className="sidebar-nav">

          <NavItem to="/dashboard" label="Dashboard" icon="▦" />

          <NavItem
            to="/transactions"
            label="Transactions"
            icon="↕"
          />

          <NavItem
            to="/import"
            label="Import Data"
            icon="↑"
          />

          <NavItem
            to="/risk"
            label="Risk Analytics"
            icon="◈"
          />

          <NavItem
            to="/backtest"
            label="Backtesting"
            icon="◫"
          />

          <NavItem
            to="/ai"
            label="AI Analyst"
            icon="✦"
          />

          <NavItem
            to="/support"
            label="Support"
            icon="?"
          />

        </nav>

        <div className="sidebar-bottom">

          <div className="logged-user">
            {localStorage.getItem("email")}
          </div>

          <button
            className="logout-btn"
            onClick={logout}
          >
            Logout
          </button>

        </div>
      </aside>

      <main className="main-content">
        {children}
      </main>

    </div>
  );
}

function NavItem({ to, label, icon }) {
  return (
    <Link
      to={to}
      className="nav-item"
    >
      <span className="nav-icon">{icon}</span>
      {label}
    </Link>
  );
}

/* =========================
   PORTFOLIO HELPER
========================= */

async function getPortfolio() {
  const data = await apiRequest("/api/portfolios");

  if (Array.isArray(data)) {
    if (data.length > 0) {
      return data[0];
    }

    return null;
  }

  return data;
}

async function ensurePortfolio() {
  let portfolio = await getPortfolio();

  if (!portfolio) {
    portfolio = await apiRequest("/api/portfolios", {
      method: "POST",
      body: JSON.stringify({
        name: "My Portfolio",
      }),
    });
  }

  return portfolio;
}

/* =========================
   DASHBOARD
========================= */

function Dashboard() {
  const [portfolio, setPortfolio] = useState(null);
  const [summary, setSummary] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [tickets, setTickets] = useState([]);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    loadDashboard();
  }, []);

  async function loadDashboard() {
    try {
      setLoading(true);

      const p = await ensurePortfolio();

      setPortfolio(p);

      const portfolioId = p.id;

      const [summaryData, transactionData, ticketData] =
        await Promise.all([
          apiRequest(
            `/api/financial-summary/${portfolioId}`
          ).catch(() => null),

          apiRequest(
            `/api/transactions/portfolio/${portfolioId}`
          ).catch(() => []),

          apiRequest(
            "/api/support-tickets"
          ).catch(() => []),
        ]);

      setSummary(summaryData);

      setTransactions(
        Array.isArray(transactionData)
          ? transactionData
          : []
      );

      setTickets(
        Array.isArray(ticketData)
          ? ticketData
          : []
      );

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (loading) {
    return <PageLoading />;
  }

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle="Your financial overview"
      />

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      <div className="portfolio-banner">
        <div>
          <span className="muted">ACTIVE PORTFOLIO</span>
          <h2>
            {portfolio?.name || "My Portfolio"}
          </h2>
        </div>

        <div className="portfolio-id">
          ID #{portfolio?.id}
        </div>
      </div>

      <div className="stats-grid">

        <StatCard
          label="Total Income"
          value={money(summary?.totalIncome)}
          type="positive"
        />

        <StatCard
          label="Total Expense"
          value={money(summary?.totalExpense)}
          type="negative"
        />

        <StatCard
          label="Net Cash Flow"
          value={money(summary?.netCashFlow)}
        />

        <StatCard
          label="Transactions"
          value={summary?.transactionCount ?? transactions.length}
        />

        <StatCard
          label="Total BUY"
          value={money(summary?.totalBuy)}
        />

        <StatCard
          label="Total SELL"
          value={money(summary?.totalSell)}
        />

      </div>

      <div className="two-column">

        <section className="panel">

          <PanelTitle
            title="Monthly Activity"
            link="/transactions"
          />

          <div className="activity-list">

            <ActivityRow
              label="Monthly Income"
              value={money(summary?.monthlyIncome)}
            />

            <ActivityRow
              label="Monthly Expense"
              value={money(summary?.monthlyExpense)}
            />

            <ActivityRow
              label="Largest Transaction"
              value={money(summary?.largestTransaction)}
            />

          </div>
        </section>

        <section className="panel">

          <PanelTitle
            title="Recent Transactions"
            link="/transactions"
          />

          {transactions.length === 0 ? (
            <EmptyState text="No transactions yet." />
          ) : (
            <TransactionMiniTable
              transactions={transactions.slice(0, 5)}
            />
          )}

        </section>

      </div>

      <div className="dashboard-bottom">

        <section className="panel">

          <PanelTitle
            title="Quick Actions"
          />

          <div className="quick-actions">

            <Link
              to="/import"
              className="quick-action"
            >
              <strong>Import Data</strong>
              <span>CSV · Excel · PDF</span>
            </Link>

            <Link
              to="/risk"
              className="quick-action"
            >
              <strong>Analyze Risk</strong>
              <span>Volatility · VaR · Drawdown</span>
            </Link>

            <Link
              to="/backtest"
              className="quick-action"
            >
              <strong>Run Backtest</strong>
              <span>Test historical performance</span>
            </Link>

            <Link
              to="/ai"
              className="quick-action"
            >
              <strong>Ask AI Analyst</strong>
              <span>Understand your portfolio</span>
            </Link>

          </div>

        </section>

        <section className="panel">

          <PanelTitle
            title="Support"
            link="/support"
          />

          <div className="support-summary">

            <div className="support-number">
              {tickets.length}
            </div>

            <div>
              <strong>Support tickets</strong>
              <span>Need help with something?</span>
            </div>

          </div>

        </section>

      </div>
    </>
  );
}

/* =========================
   TRANSACTIONS
========================= */

function Transactions() {
  const [portfolio, setPortfolio] = useState(null);
  const [transactions, setTransactions] = useState([]);

  const [date, setDate] = useState("");
  const [amount, setAmount] = useState("");
  const [type, setType] = useState("BUY");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    load();
  }, []);

  async function load() {
    try {
      const p = await ensurePortfolio();
      setPortfolio(p);

      const data =
        await apiRequest(
          `/api/transactions/portfolio/${p.id}`
        );

      setTransactions(
        Array.isArray(data) ? data : []
      );

    } catch (err) {
      setError(err.message);
    }
  }

  async function addTransaction(e) {
    e.preventDefault();

    setMessage("");
    setError("");

    try {

      await apiRequest("/api/transactions", {
        method: "POST",
        body: JSON.stringify({
          portfolioId: portfolio.id,
          transactionDate: date,
          totalAmount: Number(amount),
          transactionType: type,
        }),
      });

      setDate("");
      setAmount("");

      setMessage("Transaction added successfully.");

      await load();

    } catch (err) {
      setError(err.message);
    }
  }

  async function deleteTransaction(id) {
    if (!window.confirm("Delete this transaction?")) {
      return;
    }

    try {

      await apiRequest(
        `/api/transactions/${id}`,
        {
          method: "DELETE",
        }
      );

      await load();

    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <>
      <PageHeader
        title="Transactions"
        subtitle="Track and manage financial activity"
      />

      {message && (
        <div className="success-box">
          {message}
        </div>
      )}

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      <section className="panel">

        <PanelTitle title="Add Transaction" />

        <form
          className="transaction-form"
          onSubmit={addTransaction}
        >

          <div>
            <label>Date</label>

            <input
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
              required
            />
          </div>

          <div>
            <label>Amount</label>

            <input
              type="number"
              step="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0.00"
              required
            />
          </div>

          <div>
            <label>Type</label>

            <select
              value={type}
              onChange={(e) => setType(e.target.value)}
            >
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </div>

          <button className="btn btn-primary">
            Add Transaction
          </button>

        </form>

      </section>

      <section className="panel">

        <PanelTitle
          title={`All Transactions (${transactions.length})`}
        />

        {transactions.length === 0 ? (
          <EmptyState text="No transactions found." />
        ) : (
          <TransactionTable
            transactions={transactions}
            onDelete={deleteTransaction}
          />
        )}

      </section>
    </>
  );
}

/* =========================
   IMPORT
========================= */

function ImportPage() {

  const [portfolio, setPortfolio] = useState(null);

  const [file, setFile] = useState(null);
  const [format, setFormat] = useState("csv");

  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    ensurePortfolio()
      .then(setPortfolio)
      .catch((e) => setError(e.message));
  }, []);

  async function upload() {

    if (!file) {
      setError("Please select a file.");
      return;
    }

    if (!portfolio) {
      setError("Portfolio not available.");
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);

    try {

      const formData = new FormData();

      formData.append("file", file);

      formData.append(
        "portfolioId",
        portfolio.id
      );

      const endpoint =
        format === "csv"
          ? "/api/import/csv"
          : format === "excel"
            ? "/api/import/excel"
            : "/api/import/pdf";

      const data =
        await apiRequest(endpoint, {
          method: "POST",
          body: formData,
        });

      setResult(data);

      setFile(null);

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <>
      <PageHeader
        title="Import Financial Data"
        subtitle="Upload CSV, Excel or PDF transaction data"
      />

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {result && (
        <div className="success-box">
          <strong>
            {result.message || "Import completed"}
          </strong>

          <div className="result-grid">

            <span>
              Imported:{" "}
              {result.imported ?? 0}
            </span>

            <span>
              Duplicates:{" "}
              {result.duplicates ?? 0}
            </span>

            <span>
              Errors:{" "}
              {result.errors ?? 0}
            </span>

          </div>
        </div>
      )}

      <section className="panel">

        <PanelTitle title="Select Format" />

        <div className="format-selector">

          <button
            className={
              format === "csv"
                ? "format-card active"
                : "format-card"
            }
            onClick={() => setFormat("csv")}
          >
            <strong>CSV</strong>
            <span>Comma-separated transaction data</span>
          </button>

          <button
            className={
              format === "excel"
                ? "format-card active"
                : "format-card"
            }
            onClick={() => setFormat("excel")}
          >
            <strong>EXCEL</strong>
            <span>Microsoft Excel .xlsx files</span>
          </button>

          <button
            className={
              format === "pdf"
                ? "format-card active"
                : "format-card"
            }
            onClick={() => setFormat("pdf")}
          >
            <strong>PDF</strong>
            <span>Bank statement PDF files</span>
          </button>

        </div>

      </section>

      <section className="panel">

        <PanelTitle title={`Upload ${format.toUpperCase()}`} />

        <div className="upload-area">

          <input
            type="file"
            accept={
              format === "csv"
                ? ".csv"
                : format === "excel"
                  ? ".xlsx"
                  : ".pdf"
            }
            onChange={(e) =>
              setFile(e.target.files[0])
            }
          />

          {file && (
            <div className="selected-file">
              Selected: <strong>{file.name}</strong>
            </div>
          )}

          <button
            className="btn btn-primary btn-large"
            onClick={upload}
            disabled={loading}
          >
            {loading
              ? "Importing..."
              : `Import ${format.toUpperCase()}`}
          </button>

        </div>

        <div className="import-help">

          <strong>Expected CSV / Excel columns</strong>

          <code>
            date, amount
          </code>

          <p>
            Date should use YYYY-MM-DD format.
            Amount can be positive or negative.
          </p>

        </div>

      </section>
    </>
  );
}

/* =========================
   RISK
========================= */

function RiskAnalytics() {

  const [portfolio, setPortfolio] = useState(null);
  const [risk, setRisk] = useState(null);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    load();
  }, []);

  async function load() {

    try {

      const p = await ensurePortfolio();

      setPortfolio(p);

      const data =
        await apiRequest(
          `/api/risk/${p.id}`
        );

      setRisk(data);

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <>
      <PageHeader
        title="Risk Analytics"
        subtitle="Quantitative portfolio risk measurements"
      />

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      {loading ? (
        <PageLoading />
      ) : (
        <>
          <div className="risk-level">
            <span>PORTFOLIO RISK LEVEL</span>

            <strong className={
              risk?.riskLevel === "HIGH"
                ? "risk-high"
                : risk?.riskLevel === "MEDIUM"
                  ? "risk-medium"
                  : "risk-low"
            }>
              {risk?.riskLevel || "LOW"}
            </strong>
          </div>

          <div className="stats-grid">

            <StatCard
              label="Volatility"
              value={number(
                risk?.volatility
              )}
            />

            <StatCard
              label="VaR 95%"
              value={number(
                risk?.valueAtRisk95
              )}
            />

            <StatCard
              label="Maximum Drawdown"
              value={
                percent(
                  risk?.maxDrawdown
                )
              }
            />

            <StatCard
              label="Transactions Analyzed"
              value={
                risk?.transactionCount ?? 0
              }
            />

          </div>

          <section className="panel">

            <PanelTitle title="Risk Metrics" />

            <div className="metric-explanation">

              <Metric
                title="Volatility"
                text="Measures dispersion of transaction values. Higher volatility indicates greater variability."
              />

              <Metric
                title="Value at Risk (95%)"
                text="Estimates the magnitude of a potential loss at the 95% confidence level using historical transaction values."
              />

              <Metric
                title="Maximum Drawdown"
                text="Measures the largest observed decline from a cumulative peak."
              />

            </div>

          </section>
        </>
      )}
    </>
  );
}

/* =========================
   BACKTEST
========================= */

function Backtest() {

  const [portfolio, setPortfolio] = useState(null);
  const [result, setResult] = useState(null);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    ensurePortfolio()
      .then(setPortfolio)
      .catch((e) => setError(e.message));
  }, []);

  async function runBacktest() {

    if (!portfolio) {
      return;
    }

    setLoading(true);
    setError("");
    setResult(null);

    try {

      const data =
        await apiRequest(
          `/api/backtest/${portfolio.id}`,
          {
            method: "POST",
          }
        );

      setResult(data);

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <>
      <PageHeader
        title="Backtesting"
        subtitle="Evaluate historical transaction performance"
      />

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      <section className="panel backtest-panel">

        <div className="backtest-intro">

          <div>
            <span className="muted">
              HISTORICAL STRATEGY
            </span>

            <h2>
              Transaction Replay
            </h2>

            <p>
              Replays historical portfolio transactions
              using a starting capital of ₹100,000.
            </p>
          </div>

          <button
            className="btn btn-primary btn-large"
            onClick={runBacktest}
            disabled={loading}
          >
            {loading
              ? "Running..."
              : "Run Backtest"}
          </button>

        </div>

      </section>

      {result && (

        <div className="stats-grid">

          <StatCard
            label="Starting Capital"
            value={money(
              result.startingCapital
            )}
          />

          <StatCard
            label="Ending Capital"
            value={money(
              result.endingCapital
            )}
          />

          <StatCard
            label="Total Return"
            value={money(
              result.totalReturn
            )}
          />

          <StatCard
            label="Return %"
            value={
              number(
                result.returnPercentage
              ) + "%"
            }
          />

          <StatCard
            label="Trades"
            value={result.trades}
          />

        </div>
      )}

      <section className="panel">

        <PanelTitle title="Backtesting Pipeline" />

        <div className="pipeline">

          <PipelineStep
            number="01"
            title="Historical Data"
          />

          <PipelineStep
            number="02"
            title="Strategy"
          />

          <PipelineStep
            number="03"
            title="Signals"
          />

          <PipelineStep
            number="04"
            title="Trades"
          />

          <PipelineStep
            number="05"
            title="Portfolio Returns"
          />

          <PipelineStep
            number="06"
            title="Metrics"
          />

        </div>

      </section>
    </>
  );
}

/* =========================
   AI ANALYST
========================= */

function AIAnalyst() {

  const [portfolio, setPortfolio] = useState(null);

  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {

    ensurePortfolio()
      .then(setPortfolio)
      .catch((e) => setError(e.message));

  }, []);

  async function ask(questionOverride) {

    const q =
      questionOverride ?? question;

    if (!q.trim()) {
      return;
    }

    if (!portfolio) {
      return;
    }

    setLoading(true);
    setError("");
    setAnswer("");

    try {

      const data =
        await apiRequest(
          "/api/ai/ask",
          {
            method: "POST",
            body: JSON.stringify({
              portfolioId: portfolio.id,
              question: q,
            }),
          }
        );

      setAnswer(
        typeof data === "string"
          ? data
          : data.answer ||
            data.response ||
            JSON.stringify(data)
      );

    } catch (err) {

      setError(err.message);

    } finally {

      setLoading(false);

    }
  }

  return (
    <>
      <PageHeader
        title="AI Financial Analyst"
        subtitle="Ask questions about your financial data"
      />

      <section className="panel ai-panel">

        <div className="ai-header">
          <div className="ai-symbol">✦</div>

          <div>
            <h2>Financial Analyst</h2>

            <p>
              Ask questions and receive explanations
              based on verified portfolio data.
            </p>
          </div>
        </div>

        <div className="suggestion-grid">

          <button
            onClick={() =>
              ask("What are my total expenses?")
            }
          >
            What are my total expenses?
          </button>

          <button
            onClick={() =>
              ask("What is my net cash flow?")
            }
          >
            What is my net cash flow?
          </button>

          <button
            onClick={() =>
              ask("Analyze my recent transactions.")
            }
          >
            Analyze my recent transactions.
          </button>

          <button
            onClick={() =>
              ask("What should I understand about my portfolio?")
            }
          >
            Explain my portfolio.
          </button>

        </div>

        <div className="ai-input">

          <textarea
            value={question}
            onChange={(e) =>
              setQuestion(e.target.value)
            }
            placeholder="Ask something about your finances..."
            rows="4"
          />

          <button
            className="btn btn-primary"
            onClick={() => ask()}
            disabled={loading}
          >
            {loading
              ? "Analyzing..."
              : "Ask Analyst"}
          </button>

        </div>

        {error && (
          <div className="error-box">
            {error}
          </div>
        )}

        {answer && (

          <div className="ai-answer">

            <div className="answer-label">
              ANALYST RESPONSE
            </div>

            <div className="answer-text">
              {answer}
            </div>

          </div>

        )}

        <div className="ai-disclaimer">
          AI explanations are based on available financial
          data and are not investment advice.
        </div>

      </section>
    </>
  );
}

/* =========================
   SUPPORT
========================= */

function Support() {

  const [tickets, setTickets] = useState([]);

  const [title, setTitle] = useState("");
  const [description, setDescription] =
    useState("");

  const [priority, setPriority] =
    useState("MEDIUM");

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    loadTickets();
  }, []);

  async function loadTickets() {

    try {

      const data =
        await apiRequest(
          "/api/support-tickets"
        );

      setTickets(
        Array.isArray(data)
          ? data
          : []
      );

    } catch (err) {

      setError(err.message);

    }
  }

  async function createTicket(e) {

    e.preventDefault();

    try {

      await apiRequest(
        "/api/support-tickets",
        {
          method: "POST",
          body: JSON.stringify({
            issueTitle: title,
            description,
            priority,
          }),
        }
      );

      setTitle("");
      setDescription("");
      setPriority("MEDIUM");

      setMessage(
        "Support ticket created."
      );

      await loadTickets();

    } catch (err) {

      setError(err.message);

    }
  }

  async function resolveTicket(id) {

    const resolution =
      window.prompt(
        "Enter resolution:"
      );

    if (!resolution) {
      return;
    }

    try {

      await apiRequest(
        `/api/support-tickets/${id}/resolve`,
        {
          method: "PUT",
          body: JSON.stringify({
            resolution,
          }),
        }
      );

      await loadTickets();

    } catch (err) {

      setError(err.message);

    }
  }

  return (
    <>
      <PageHeader
        title="Support"
        subtitle="Create and track support requests"
      />

      {message && (
        <div className="success-box">
          {message}
        </div>
      )}

      {error && (
        <div className="error-box">
          {error}
        </div>
      )}

      <section className="panel">

        <PanelTitle title="Create Support Ticket" />

        <form
          className="support-form"
          onSubmit={createTicket}
        >

          <label>Issue Title</label>

          <input
            value={title}
            onChange={(e) =>
              setTitle(e.target.value)
            }
            placeholder="What went wrong?"
            required
          />

          <label>Description</label>

          <textarea
            value={description}
            onChange={(e) =>
              setDescription(e.target.value)
            }
            placeholder="Describe the issue..."
            rows="5"
            required
          />

          <label>Priority</label>

          <select
            value={priority}
            onChange={(e) =>
              setPriority(e.target.value)
            }
          >
            <option value="LOW">LOW</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HIGH">HIGH</option>
          </select>

          <button className="btn btn-primary">
            Create Ticket
          </button>

        </form>

      </section>

      <section className="panel">

        <PanelTitle
          title={`My Tickets (${tickets.length})`}
        />

        {tickets.length === 0 ? (
          <EmptyState text="No support tickets." />
        ) : (

          <div className="ticket-list">

            {tickets.map((ticket) => (

              <div
                className="ticket-card"
                key={ticket.id}
              >

                <div className="ticket-top">

                  <div>
                    <h3>
                      {ticket.issueTitle}
                    </h3>

                    <p>
                      {ticket.description}
                    </p>
                  </div>

                  <StatusBadge
                    status={ticket.status}
                  />

                </div>

                <div className="ticket-meta">

                  <span>
                    Priority:{" "}
                    <strong>
                      {ticket.priority}
                    </strong>
                  </span>

                  {ticket.createdAt && (
                    <span>
                      {new Date(
                        ticket.createdAt
                      ).toLocaleDateString()}
                    </span>
                  )}

                </div>

                {ticket.status !== "RESOLVED" && (

                  <button
                    className="btn btn-secondary"
                    onClick={() =>
                      resolveTicket(ticket.id)
                    }
                  >
                    Resolve
                  </button>

                )}

                {ticket.resolution && (

                  <div className="resolution">
                    <strong>Resolution:</strong>{" "}
                    {ticket.resolution}
                  </div>

                )}

              </div>

            ))}

          </div>
        )}

      </section>
    </>
  );
}

/* =========================
   REUSABLE COMPONENTS
========================= */

function PageHeader({ title, subtitle }) {
  return (
    <header className="page-header">

      <div>
        <div className="page-kicker">
          WHEREISMONEY
        </div>

        <h1>{title}</h1>

        <p>{subtitle}</p>
      </div>

      <div className="header-user">
        {localStorage.getItem("email")}
      </div>

    </header>
  );
}

function PanelTitle({ title, link }) {
  return (
    <div className="panel-title">

      <h2>{title}</h2>

      {link && (
        <Link to={link}>
          View all →
        </Link>
      )}

    </div>
  );
}

function StatCard({
  label,
  value,
  type = "",
}) {
  return (
    <div className={`stat-card ${type}`}>

      <span>{label}</span>

      <strong>{value ?? "₹0"}</strong>

    </div>
  );
}

function ActivityRow({ label, value }) {
  return (
    <div className="activity-row">

      <span>{label}</span>

      <strong>{value}</strong>

    </div>
  );
}

function TransactionMiniTable({ transactions }) {
  return (
    <div className="table-wrapper">

      <table>

        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Amount</th>
          </tr>
        </thead>

        <tbody>

          {transactions.map((t) => (

            <tr key={t.id}>

              <td>
                {t.transactionDate || "-"}
              </td>

              <td>
                <span className="type-badge">
                  {t.transactionType || "-"}
                </span>
              </td>

              <td>
                {money(t.totalAmount)}
              </td>

            </tr>

          ))}

        </tbody>

      </table>

    </div>
  );
}

function TransactionTable({
  transactions,
  onDelete,
}) {
  return (
    <div className="table-wrapper">

      <table>

        <thead>

          <tr>
            <th>ID</th>
            <th>Date</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Action</th>
          </tr>

        </thead>

        <tbody>

          {transactions.map((t) => (

            <tr key={t.id}>

              <td>#{t.id}</td>

              <td>
                {t.transactionDate || "-"}
              </td>

              <td>
                <span className="type-badge">
                  {t.transactionType || "-"}
                </span>
              </td>

              <td>
                {money(t.totalAmount)}
              </td>

              <td>

                <button
                  className="delete-btn"
                  onClick={() =>
                    onDelete(t.id)
                  }
                >
                  Delete
                </button>

              </td>

            </tr>

          ))}

        </tbody>

      </table>

    </div>
  );
}

function StatusBadge({ status }) {
  return (
    <span
      className={`status-badge ${
        status?.toLowerCase()
      }`}
    >
      {status}
    </span>
  );
}

function Metric({ title, text }) {
  return (
    <div className="metric">

      <strong>{title}</strong>

      <span>{text}</span>

    </div>
  );
}

function PipelineStep({ number, title }) {
  return (
    <div className="pipeline-step">

      <span>{number}</span>

      <strong>{title}</strong>

    </div>
  );
}

function EmptyState({ text }) {
  return (
    <div className="empty-state">
      {text}
    </div>
  );
}

function PageLoading() {
  return (
    <div className="page-loading">
      Loading...
    </div>
  );
}

/* =========================
   FORMATTERS
========================= */

function money(value) {

  if (
    value === null ||
    value === undefined ||
    value === ""
  ) {
    return "₹0.00";
  }

  const n = Number(value);

  if (Number.isNaN(n)) {
    return "₹0.00";
  }

  return `₹${n.toLocaleString("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}`;
}

function number(value) {

  if (
    value === null ||
    value === undefined
  ) {
    return "0";
  }

  const n = Number(value);

  return Number.isNaN(n)
    ? "0"
    : n.toLocaleString("en-IN", {
        maximumFractionDigits: 2,
      });
}

function percent(value) {

  if (
    value === null ||
    value === undefined
  ) {
    return "0%";
  }

  return `${(
    Number(value) * 100
  ).toFixed(2)}%`;
}

/* =========================
   ROUTER
========================= */

function AppRoutes() {
  return (
    <Routes>

      <Route
        path="/"
        element={<Landing />}
      />

      <Route
        path="/login"
        element={<Login />}
      />

      <Route
        path="/register"
        element={<Register />}
      />

      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout>
              <Dashboard />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/transactions"
        element={
          <ProtectedRoute>
            <Layout>
              <Transactions />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/import"
        element={
          <ProtectedRoute>
            <Layout>
              <ImportPage />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/risk"
        element={
          <ProtectedRoute>
            <Layout>
              <RiskAnalytics />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/backtest"
        element={
          <ProtectedRoute>
            <Layout>
              <Backtest />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/ai"
        element={
          <ProtectedRoute>
            <Layout>
              <AIAnalyst />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="/support"
        element={
          <ProtectedRoute>
            <Layout>
              <Support />
            </Layout>
          </ProtectedRoute>
        }
      />

      <Route
        path="*"
        element={<Navigate to="/" replace />}
      />

    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
  );
}