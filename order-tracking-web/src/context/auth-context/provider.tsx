import React, { useState } from 'react';
import { AuthContext } from './context';
import { loginApi } from '@/api/login';

const getErrorMessage = (err: unknown, fallback: string): string => {
  if (err instanceof Error) return err.message;
  if (typeof err === 'string') return err;
  return fallback;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => {
    return localStorage.getItem('userIsLoggedUI') === 'true';
  });

  const login = async (email: string, password: string) => {
    try {
      await loginApi.login({ email, password });
      localStorage.setItem('userIsLoggedUI', 'true');
      setIsAuthenticated(true);
    } catch (err) {
      throw new Error(getErrorMessage(err, 'Erro ao realizar login.'), {
        cause: err,
      });
    }
  };

  const register = async (name: string, email: string, password: string) => {
    try {
      await loginApi.register({ name, email, password });
    } catch (err) {
      throw new Error(getErrorMessage(err, 'Erro ao realizar cadastro.'), {
        cause: err,
      });
    }
  };

  const logout = async () => {
    try {
      await loginApi.logout();
    } catch (err) {
      throw new Error(getErrorMessage(err, 'Houve um erro ao sair'), {
        cause: err,
      });
    } finally {
      localStorage.removeItem('userIsLoggedUI');
      setIsAuthenticated(false);
    }
  };

  return (
    <AuthContext.Provider value={{ isAuthenticated, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
};
