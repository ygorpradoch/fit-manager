import { useState, type FormEvent } from "react";
import { useNavigate, Navigate } from "react-router-dom";
import { login, fakeLogin } from "../../services/authService";
import { useAuth } from "../../hooks/useAuth";
import type { LoginCredentials } from "../../types/auth.types";
import "../auth/Login.css";

export function LoginPage() {
  const navigate = useNavigate();
  const { isAuth, dashboardPath } = useAuth();

  const [credentials, setCredentials] = useState<LoginCredentials>({
    email: "",
    senha: "",
  });
  const [erro, setErro] = useState<string>("");
  const [loading, setLoading] = useState<boolean>(false);

  if (isAuth) {
    return <Navigate to={dashboardPath} replace />;
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setCredentials((prev) => ({ ...prev, [name]: value }));
  };

  const handleLogin = async (e: FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault();
    setErro("");
    setLoading(true);

    try {
      const { redirectTo } = await login(credentials);
      navigate(redirectTo, { replace: true });
    } catch (err: unknown) {
      if (err instanceof Error) {
        setErro(err.message);
      } else {
        setErro("Ocorreu um erro inesperado.");
      }
    } finally {
      setLoading(false);
    }
  };

  const handleQuickLogin = (perfil: "ADMIN" | "INSTRUTOR" | "ALUNO"): void => {
    const { redirectTo } = fakeLogin(perfil);
    navigate(redirectTo, { replace: true });
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <div className="login-logo">
          <h1>FIT</h1>
          <span>MANAGER</span>
        </div>

        <h2 className="login-title">Sistema de Gerenciamento</h2>
        <p style={{ textAlign: "center", fontSize: "0.85rem", opacity: 0.7 }}>
          Protótipo — nenhuma credencial é validada.
        </p>

        <form onSubmit={handleLogin} className="login-form">
          <div className="form-group">
            <label htmlFor="email">Usuário</label>
            <input
              id="email"
              type="email"
              name="email"
              placeholder="Digite seu e-mail (opcional)"
              value={credentials.email}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="senha">Senha</label>
            <input
              id="senha"
              type="password"
              name="senha"
              placeholder="Digite sua senha (opcional)"
              value={credentials.senha}
              onChange={handleChange}
            />
          </div>

          {erro && <p className="erro">{erro}</p>}

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? "Entrando..." : "Entrar"}
          </button>
        </form>

        <div style={{ marginTop: "1.5rem", display: "flex", flexDirection: "column", gap: "0.5rem" }}>
          <span style={{ fontSize: "0.85rem", opacity: 0.7, textAlign: "center" }}>
            Ou entre direto como:
          </span>
          <div style={{ display: "flex", gap: "0.5rem", justifyContent: "center" }}>
            <button type="button" className="login-button" onClick={() => handleQuickLogin("ADMIN")}>
              Admin
            </button>
            <button type="button" className="login-button" onClick={() => handleQuickLogin("INSTRUTOR")}>
              Instrutor
            </button>
            <button type="button" className="login-button" onClick={() => handleQuickLogin("ALUNO")}>
              Aluno
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}