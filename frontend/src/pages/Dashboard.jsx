import React from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { Scale, MessageSquare, Award, BookOpen, User, PlusCircle, ArrowRight } from "lucide-react";
import Navbar from "../components/Navbar";
import api from "../services/api";
import { useTranslation } from "react-i18next";

const Dashboard = () => {
  const { user } = useAuth();
  const { t } = useTranslation();

  const getPhotoUrl = (path) => {
    if (!path) return null;
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    const baseUrl = api?.defaults?.baseURL || "http://localhost:8080/api";
    const host = baseUrl.replace("/api", "");
    return `${host}${path}`;
  };

  const isOfficerOrAdmin = user?.role === "ADMIN" || user?.role === "GOVERNMENT_OFFICER";

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col">
      <Navbar />

      <main className="flex-grow px-6 md:px-12 py-10 max-w-6xl mx-auto w-full">
        {/* Welcome Section */}
        <div className="glass-panel p-8 rounded-2xl mb-8 flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
          <div className="flex flex-col sm:flex-row items-start sm:items-center gap-4 text-left">
            {user?.profilePhotoUrl ? (
              <img
                src={getPhotoUrl(user.profilePhotoUrl)}
                className="h-16 w-16 rounded-full object-cover border-2 border-brand-500/30 flex-shrink-0"
                alt="Avatar"
              />
            ) : (
              <div className="h-16 w-16 rounded-full bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-500 font-extrabold text-2xl uppercase flex-shrink-0">
                {user?.fullName?.charAt(0)}
              </div>
            )}
            <div className="space-y-1">
              <h2 className="text-3xl font-extrabold text-white">
                {t("dashboard.welcome", { name: user?.fullName })}
              </h2>
              <p className="text-slate-400 text-sm">
                {t("dashboard.modules_desc")}
              </p>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
            <Link
              to="/profile"
              className="btn-glass-secondary flex items-center gap-1.5 px-4 py-2.5 rounded-xl text-xs font-semibold cursor-pointer"
            >
              <User className="h-4 w-4" /> {t("common.edit")} {t("common.profile")}
            </Link>
            <div className="bg-white/5 border border-white/10 rounded-xl p-4 flex items-center gap-3">
              <div className="h-10 w-10 rounded-lg bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-500">
                <User className="h-5 w-5" />
              </div>
              <div className="text-left">
                <div className="text-xs text-slate-400">{t("dashboard.account_type")}</div>
                <div className="text-sm font-bold text-white uppercase tracking-wider">
                  {user?.role.replace("_", " ")}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Dynamic Admin/Officer Quick Actions */}
        {isOfficerOrAdmin && (
          <div className="bg-brand-500/10 border border-brand-500/25 rounded-2xl p-6 mb-8 text-left">
            <h3 className="text-lg font-bold text-white mb-2 flex items-center gap-2">
              <PlusCircle className="h-5 w-5 text-brand-500" />
              {t("dashboard.officer_controls")}
            </h3>
            <p className="text-xs text-slate-300 mb-4">
              {t("dashboard.officer_desc")}
            </p>
            <div className="flex flex-wrap gap-4">
              <Link
                to="/schemes?action=create"
                className="btn-glass-primary flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold cursor-pointer"
              >
                <PlusCircle className="h-4 w-4" /> {t("dashboard.add_scheme")}
              </Link>
              <Link
                to="/scholarships?action=create"
                className="btn-glass-secondary flex items-center gap-1.5 px-4 py-2 rounded-lg text-xs font-semibold cursor-pointer"
              >
                <PlusCircle className="h-4 w-4" /> {t("dashboard.add_scholarship")}
              </Link>
            </div>
          </div>
        )}

        {/* Quick Launch Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 text-left">
          {/* Card 1 */}
          <div className="glass-panel p-6 rounded-2xl flex flex-col">
            <MessageSquare className="h-8 w-8 text-brand-500 mb-4" />
            <h3 className="text-lg font-bold text-white mb-2">{t("common.ai_assist")}</h3>
            <p className="text-slate-400 text-xs leading-relaxed mb-6">
              {t("dashboard.chat_desc")}
            </p>
            <Link
              to="/chat"
              className="mt-auto flex items-center gap-1.5 text-xs font-semibold text-brand-500 hover:text-brand-400 transition-colors group cursor-pointer"
            >
              {t("dashboard.start_chatting")}
              <ArrowRight className="h-3.5 w-3.5 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>

          {/* Card 2 */}
          <div className="glass-panel p-6 rounded-2xl flex flex-col">
            <Award className="h-8 w-8 text-accent-teal mb-4" />
            <h3 className="text-lg font-bold text-white mb-2">{t("common.schemes")}</h3>
            <p className="text-slate-400 text-xs leading-relaxed mb-6">
              {t("dashboard.schemes_desc")}
            </p>
            <Link
              to="/schemes"
              className="mt-auto flex items-center gap-1.5 text-xs font-semibold text-accent-teal hover:text-teal-400 transition-colors group cursor-pointer"
            >
              {t("common.explore_schemes")}
              <ArrowRight className="h-3.5 w-3.5 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>

          {/* Card 3 */}
          <div className="glass-panel p-6 rounded-2xl flex flex-col">
            <BookOpen className="h-8 w-8 text-accent-indigo mb-4" />
            <h3 className="text-lg font-bold text-white mb-2">{t("common.scholarships")}</h3>
            <p className="text-slate-400 text-xs leading-relaxed mb-6">
              {t("dashboard.scholarships_desc")}
            </p>
            <Link
              to="/scholarships"
              className="mt-auto flex items-center gap-1.5 text-xs font-semibold text-accent-indigo hover:text-indigo-400 transition-colors group cursor-pointer"
            >
              {t("dashboard.find_scholarships")}
              <ArrowRight className="h-3.5 w-3.5 group-hover:translate-x-1 transition-transform" />
            </Link>
          </div>
        </div>

        {/* Suggestion / Tip box */}
        <div className="glass-panel p-6 rounded-2xl mt-8 text-left border border-white/5 bg-slate-950/40">
          <h4 className="text-xs font-bold text-slate-300 uppercase tracking-wider mb-2">{t("dashboard.quick_tip")}</h4>
          <p className="text-xs text-slate-400 leading-relaxed">
            {t("dashboard.quick_tip_desc")}
          </p>
        </div>
      </main>

      <footer className="border-t border-white/5 py-6 text-center text-xs text-slate-500">
        <p>&copy; {new Date().getFullYear()} {t("profile.session_info")}</p>
      </footer>
    </div>
  );
};

export default Dashboard;
