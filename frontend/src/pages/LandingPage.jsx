import React from "react";
import { Link } from "react-router-dom";
import { Scale, MessageSquare, Award, BookOpen, ChevronRight, CheckCircle2 } from "lucide-react";
import Navbar from "../components/Navbar";
import { useTranslation } from "react-i18next";

const LandingPage = () => {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col">
      <Navbar />

      {/* Hero Section */}
      <section className="relative px-6 md:px-12 pt-20 pb-24 md:pt-32 md:pb-40 max-w-6xl mx-auto text-center flex-grow">
        {/* Decorative background glows */}
        <div className="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-72 h-72 md:w-96 md:h-96 rounded-full bg-brand-500/10 blur-[80px] -z-10 animate-pulse-slow"></div>
        <div className="absolute top-1/3 left-1/3 w-60 h-60 rounded-full bg-accent-teal/5 blur-[90px] -z-10"></div>

        <div className="animate-slide-up">
          <div className="inline-flex items-center gap-2 bg-brand-500/10 border border-brand-500/25 px-4.5 py-1.5 rounded-full text-xs font-semibold text-brand-200 mb-6">
            <Scale className="h-3.5 w-3.5" />
            {t("landing.badge")}
          </div>

          <h1 className="text-4xl md:text-6xl font-extrabold tracking-tight text-white mb-6 leading-tight max-w-4xl mx-auto">
            {t("landing.title")}
          </h1>

          <p className="text-lg md:text-xl text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed">
            {t("landing.subtitle")}
          </p>

          <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link
              to="/register"
              className="w-full sm:w-auto btn-glass-primary flex items-center justify-center gap-2 px-8 py-3.5 rounded-xl font-semibold shadow-lg hover:shadow-brand-500/20 cursor-pointer"
            >
              {t("common.get_started")}
              <ChevronRight className="h-4 w-4" />
            </Link>
            <Link
              to="/login"
              className="w-full sm:w-auto btn-glass-secondary flex items-center justify-center px-8 py-3.5 rounded-xl font-semibold cursor-pointer"
            >
              {t("common.login")}
            </Link>
          </div>
        </div>

        {/* Feature Cards Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mt-24 md:mt-32">
          {/* Card 1 */}
          <div className="glass-panel glass-panel-hover p-8 rounded-2xl flex flex-col items-start text-left">
            <div className="h-12 w-12 rounded-xl bg-brand-500/10 border border-brand-500/20 flex items-center justify-center mb-6">
              <MessageSquare className="h-6 w-6 text-brand-500" />
            </div>
            <h3 className="text-xl font-bold text-white mb-3">{t("landing.feat_ai_title")}</h3>
            <p className="text-slate-400 text-sm leading-relaxed mb-6">
              {t("landing.feat_ai_desc")}
            </p>
            <ul className="space-y-2 mt-auto text-xs text-slate-300">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-accent-teal" /> {t("landing.feat_ngo_title")}
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-accent-teal" /> {t("landing.feat_legal_title")}
              </li>
            </ul>
          </div>

          {/* Card 2 */}
          <div className="glass-panel glass-panel-hover p-8 rounded-2xl flex flex-col items-start text-left">
            <div className="h-12 w-12 rounded-xl bg-accent-teal/10 border border-accent-teal/20 flex items-center justify-center mb-6">
              <Award className="h-6 w-6 text-accent-teal" />
            </div>
            <h3 className="text-xl font-bold text-white mb-3">{t("common.schemes")}</h3>
            <p className="text-slate-400 text-sm leading-relaxed mb-6">
              {t("landing.feat_recs_desc")}
            </p>
            <ul className="space-y-2 mt-auto text-xs text-slate-300">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-brand-500" /> {t("landing.feat_recs_title")}
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-brand-500" /> {t("landing.feat_links_title")}
              </li>
            </ul>
          </div>

          {/* Card 3 */}
          <div className="glass-panel glass-panel-hover p-8 rounded-2xl flex flex-col items-start text-left">
            <div className="h-12 w-12 rounded-xl bg-accent-indigo/10 border border-accent-indigo/20 flex items-center justify-center mb-6">
              <BookOpen className="h-6 w-6 text-accent-indigo" />
            </div>
            <h3 className="text-xl font-bold text-white mb-3">{t("common.scholarships")}</h3>
            <p className="text-slate-400 text-sm leading-relaxed mb-6">
              {t("landing.feat_secure_desc")}
            </p>
            <ul className="space-y-2 mt-auto text-xs text-slate-300">
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-accent-teal" /> {t("landing.feat_secure_title")}
              </li>
              <li className="flex items-center gap-2">
                <CheckCircle2 className="h-3.5 w-3.5 text-accent-teal" /> {t("landing.feat_links_desc")}
              </li>
            </ul>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-white/5 py-8 text-center text-xs text-slate-500">
        <p>&copy; {new Date().getFullYear()} {t("landing.footer_info")}</p>
      </footer>
    </div>
  );
};

export default LandingPage;
