import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import toast from 'react-hot-toast';
import { authService } from '../services/authService';
import { clearAuthToken, getAuthToken, hasAuthToken, setAuthToken } from '../utils/authStorage';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [isReady, setIsReady] = useState(false);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);

  const fetchMe = useCallback(async () => {
    if (!hasAuthToken()) {
      setIsAuthenticated(false);
      setUser(null);
      return;
    }

    try {
      const { data } = await authService.me();
      setUser(data);
      setIsAuthenticated(true);
    } catch (error) {
      clearAuthToken();
      setIsAuthenticated(false);
      setUser(null);
      throw error;
    }
  }, []);

  const refreshSession = useCallback(async () => {
    const { data } = await authService.refresh();
    if (data?.token) {
      setAuthToken(data.token);
      setUser(data.user);
      setIsAuthenticated(true);
      return data;
    }
    throw new Error('Refresh did not return a token');
  }, []);

  useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        if (hasAuthToken()) {
          try {
            await fetchMe();
          } catch {
            await refreshSession();
          }
        } else {
          await refreshSession();
        }
      } catch {
        clearAuthToken();
        setIsAuthenticated(false);
        setUser(null);
      }

      if (mounted) {
        setIsReady(true);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [fetchMe, refreshSession]);

  const login = useCallback(async (payload) => {
    const { data } = await authService.login(payload);
    setAuthToken(data.token);
    setUser(data.user);
    setIsAuthenticated(true);
    return data;
  }, []);

  const register = useCallback(async (payload) => {
    const { data } = await authService.register(payload);
    setAuthToken(data.token);
    setUser(data.user);
    setIsAuthenticated(true);
    return data;
  }, []);

  const completeOAuthLogin = useCallback(async (token) => {
    if (!token) {
      throw new Error('Missing OAuth token');
    }

    setAuthToken(token);
    await fetchMe();
  }, [fetchMe]);

  const logout = useCallback(() => {
    authService.logout().catch(() => null);
    clearAuthToken();
    setIsAuthenticated(false);
    setUser(null);
    toast.success('Signed out successfully');
  }, []);

  const value = useMemo(() => ({
    isReady,
    isAuthenticated,
    user,
    token: getAuthToken(),
    login,
    register,
    completeOAuthLogin,
    refreshSession,
    logout,
  }), [completeOAuthLogin, isAuthenticated, isReady, login, logout, refreshSession, register, user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return ctx;
}
