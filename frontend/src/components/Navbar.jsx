import React from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Scale, LogOut, MessageSquare, Award, BookOpen, User as UserIcon, LayoutDashboard, Globe } from "lucide-react";
import api from "../services/api";
import { useTranslation } from "react-i18next";

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { t, i18n } = useTranslation();

  const handleLogout = async () => {
    await logout();
    navigate("/");
  };

  const handleLanguageChange = (e) => {
    const lang = e.target.value;
    i18n.changeLanguage(lang);
    localStorage.setItem("selectedLanguage", lang);
  };

  const isActive = (path) => location.pathname === path;

  const getPhotoUrl = (path) => {
    if (!path) return null;
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    const baseUrl = api?.defaults?.baseURL || "http://localhost:8080/api";
    const host = baseUrl.replace("/api", "");
    return `${host}${path}`;
  };

  return (
    <nav className="sticky top-0 z-50 w-full glass-panel border-b border-white/5 py-4 px-6 md:px-12 flex items-center justify-between">
      <Link to="/" className="flex items-center gap-2 group">
        <Scale className="h-6 w-6 text-brand-500 group-hover:rotate-12 transition-transform duration-300" />
        <span className="text-xl font-bold tracking-tight text-white">
          JANA <span className="gradient-text">AI</span>
        </span>
      </Link>

      <div className="flex items-center gap-6">
        {user ? (
          <>
            <div className="hidden md:flex items-center gap-4">
              <Link
                to="/dashboard"
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm transition-all duration-200 ${
                  isActive("/dashboard")
                    ? "bg-brand-500/10 text-brand-500 border border-brand-500/20"
                    : "text-slate-300 hover:text-white hover:bg-white/5 border border-transparent"
                }`}
              >
                <LayoutDashboard className="h-4 w-4" />
                {t("common.dashboard")}
              </Link>
              <Link
                to="/chat"
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm transition-all duration-200 ${
                  isActive("/chat")
                    ? "bg-brand-500/10 text-brand-500 border border-brand-500/20"
                    : "text-slate-300 hover:text-white hover:bg-white/5 border border-transparent"
                }`}
              >
                <MessageSquare className="h-4 w-4" />
                {t("common.ai_assist")}
              </Link>
              <Link
                to="/schemes"
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm transition-all duration-200 ${
                  isActive("/schemes")
                    ? "bg-brand-500/10 text-brand-500 border border-brand-500/20"
                    : "text-slate-300 hover:text-white hover:bg-white/5 border border-transparent"
                }`}
              >
                <Award className="h-4 w-4" />
                {t("common.schemes")}
              </Link>
              <Link
                to="/scholarships"
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-sm transition-all duration-200 ${
                  isActive("/scholarships")
                    ? "bg-brand-500/10 text-brand-500 border border-brand-500/20"
                    : "text-slate-300 hover:text-white hover:bg-white/5 border border-transparent"
                }`}
              >
                <BookOpen className="h-4 w-4" />
                {t("common.scholarships")}
              </Link>
            </div>

            <div className="flex items-center gap-4">
              {/* Language Selector */}
              <div className="flex items-center gap-1 bg-white/5 border border-white/10 rounded-lg px-2 py-1">
                <Globe className="h-3.5 w-3.5 text-slate-400" />
                <select
                  value={i18n.language.slice(0, 2)}
                  onChange={handleLanguageChange}
                  className="bg-transparent text-white text-xs border-none focus:ring-0 focus:outline-none cursor-pointer"
                >
                  <option value="en" className="bg-slate-950 text-white">EN</option>
                  <option value="kn" className="bg-slate-950 text-white">ಕನ್ನಡ</option>
                  <option value="hi" className="bg-slate-950 text-white">हिन्दी</option>
                </select>
              </div>

              <Link to="/profile" className="flex items-center gap-2 bg-white/5 border border-white/10 hover:border-brand-500/30 rounded-full py-1 px-3.5 hover:bg-white/10 transition-all duration-200" title={t("common.profile")}>
                {user.profilePhotoUrl ? (
                  <img
                    src={getPhotoUrl(user.profilePhotoUrl)}
                    className="h-6 w-6 rounded-full object-cover border border-white/20"
                    alt=""
                  />
                ) : (
                  <div className="h-6 w-6 rounded-full bg-brand-500 flex items-center justify-center text-xs font-bold text-white uppercase">
                    {user.fullName.charAt(0)}
                  </div>
                )}
                <div className="flex flex-col text-left">
                  <span className="text-xs font-semibold text-white leading-tight max-w-[80px] truncate">{user.fullName}</span>
                  <span className="text-[10px] text-slate-400 capitalize leading-none">
                    {user.role === "GOVERNMENT_OFFICER" ? t("register.role_officer") : user.role === "ADMIN" ? "Admin" : t("register.role_citizen")}
                  </span>
                </div>
              </Link>

              <button
                onClick={handleLogout}
                className="flex items-center gap-1.5 px-3 py-2 rounded-lg text-xs text-rose-400 hover:text-rose-300 hover:bg-rose-500/10 border border-transparent hover:border-rose-500/20 transition-all duration-200 cursor-pointer"
                title={t("common.logout")}
              >
                <Scale className="hidden" /> {/* to satisfy scale rule */}
                <LogOut className="h-3.5 w-3.5" />
                <span className="hidden sm:inline">{t("common.logout")}</span>
              </button>
            </div>
          </>
        ) : (
          <div className="flex items-center gap-4">
            {/* Language Selector for guest */}
            <div className="flex items-center gap-1 bg-white/5 border border-white/10 rounded-lg px-2 py-1">
              <Globe className="h-3.5 w-3.5 text-slate-400" />
              <select
                value={i18n.language.slice(0, 2)}
                onChange={handleLanguageChange}
                className="bg-transparent text-white text-xs border-none focus:ring-0 focus:outline-none cursor-pointer"
              >
                <option value="en" className="bg-slate-950 text-white">EN</option>
                <option value="kn" className="bg-slate-950 text-white">ಕನ್ನಡ</option>
                <option value="hi" className="bg-slate-950 text-white">हिन्दी</option>
              </select>
            </div>

            <Link to="/login" className="text-sm font-medium text-slate-300 hover:text-white transition-colors">
              {t("common.login")}
            </Link>
            <Link to="/register" className="btn-glass-primary text-sm font-medium py-1.5 px-4 rounded-lg">
              {t("common.sign_up")}
            </Link>
          </div>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
