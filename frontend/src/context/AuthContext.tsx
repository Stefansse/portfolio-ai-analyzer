import React, { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

// ---------------- JWT Utils ----------------
const decodeToken = (token: string) => {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload;
  } catch (err) {
    console.error("Invalid JWT", err);
    return null;
  }
};

const getEmailFromToken = (token: string) => {
  const payload = decodeToken(token);
  if (!payload) return null;
  return payload.sub || null; // backend uses 'sub' for email
};

// ---------------- Auth Context ----------------
interface AuthContextType {
  token: string | null;
  email: string | null;
  userId: string | null;
  setAuth: (token: string, userId: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const navigate = useNavigate();

  const storedToken = localStorage.getItem("jwtToken");
  const storedUserId = localStorage.getItem("userId");

  const [token, setToken] = useState<string | null>(storedToken);
  const [userId, setUserId] = useState<string | null>(storedUserId);
  const [email, setEmail] = useState<string | null>(
    storedToken ? getEmailFromToken(storedToken) : null
  );

  const setAuth = (newToken: string, newUserId: string) => {
    localStorage.setItem("jwtToken", newToken);
    localStorage.setItem("userId", newUserId);

    setToken(newToken);
    setUserId(newUserId);
    setEmail(getEmailFromToken(newToken));
  };

  const logout = () => {
    localStorage.removeItem("jwtToken");
    localStorage.removeItem("userId");

    setToken(null);
    setUserId(null);
    setEmail(null);
    navigate("/login");
  };

  return (
    <AuthContext.Provider value={{ token, email, userId, setAuth, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
