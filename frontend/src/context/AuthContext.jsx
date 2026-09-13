import React, { createContext, useContext, useState, useEffect } from "react";
import api from "../services/api";
import i18n from "../i18n";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check if user credentials exist in local storage on boot
    const storedUser = localStorage.getItem("user");
    const token = localStorage.getItem("accessToken");
    if (storedUser && token) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        localStorage.removeItem("user");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    setLoading(true);
    try {
      const response = await api.post("/auth/login", { email, password });
      const authData = response.data.data;
      
      const loggedUser = {
        id: authData.id,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        profilePhotoUrl: authData.profilePhotoUrl,
      };

      localStorage.setItem("accessToken", authData.accessToken);
      localStorage.setItem("refreshToken", authData.refreshToken);
      localStorage.setItem("user", JSON.stringify(loggedUser));
      
      setUser(loggedUser);
      return loggedUser;
    } catch (error) {
      throw error.response?.data?.message || i18n.t("login.invalid_credentials");
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    setLoading(true);
    try {
      const response = await api.post("/auth/register", userData);
      const authData = response.data.data;

      const loggedUser = {
        id: authData.id,
        fullName: authData.fullName,
        email: authData.email,
        role: authData.role,
        profilePhotoUrl: authData.profilePhotoUrl,
      };

      localStorage.setItem("accessToken", authData.accessToken);
      localStorage.setItem("refreshToken", authData.refreshToken);
      localStorage.setItem("user", JSON.stringify(loggedUser));

      setUser(loggedUser);
      return loggedUser;
    } catch (error) {
      throw error.response?.data?.message || i18n.t("register.registration_failed");
    } finally {
      setLoading(false);
    }
  };

  const logout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      try {
        // Send logout request to revoke the refresh token in the database
        await api.post("/auth/logout", { refreshToken });
      } catch (e) {
        console.error("Error during backend logout", e);
      }
    }
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
  };

  const updateUser = (updatedUserFields) => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      try {
        const parsed = JSON.parse(storedUser);
        const merged = { ...parsed, ...updatedUserFields };
        localStorage.setItem("user", JSON.stringify(merged));
        setUser(merged);
      } catch (e) {
        console.error("Failed to parse user in localStorage", e);
      }
    } else {
      setUser(updatedUserFields);
    }
  };

  const value = {
    user,
    loading,
    login,
    register,
    logout,
    updateUser,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
};
