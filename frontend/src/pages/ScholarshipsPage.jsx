import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import { BookOpen, Search, Filter, Plus, Edit, Trash2, Globe, FileText, ChevronLeft, ChevronRight, X, Loader2, Calendar, CheckCircle2 } from "lucide-react";
import Navbar from "../components/Navbar";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

const ScholarshipsPage = () => {
  const { user } = useAuth();
  const location = useLocation();
  const { t, i18n } = useTranslation();

  // Core Scholarships State
  const [scholarships, setScholarships] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters State
  const [search, setSearch] = useState("");
  const [educationLevel, setEducationLevel] = useState("");
  const [category, setCategory] = useState("");
  const [income, setIncome] = useState("");
  const [stateFilter, setStateFilter] = useState("");

  // CRUD Form Dialog State
  const [showFormModal, setShowFormModal] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [formId, setFormId] = useState(null);

  const [formName, setFormName] = useState("");
  const [formCategory, setFormCategory] = useState("");
  const [formEducationLevel, setFormEducationLevel] = useState("");
  const [formIncomeLimit, setFormIncomeLimit] = useState("");
  const [formState, setFormState] = useState("ALL");
  const [formEligibility, setFormEligibility] = useState("");
  const [formAmount, setFormAmount] = useState("");
  const [formDeadline, setFormDeadline] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formOfficialWebsite, setFormOfficialWebsite] = useState("");
  const [formApplyLink, setFormApplyLink] = useState("");
  const [adminSuccessMsg, setAdminSuccessMsg] = useState("");

  // Check query params on mount for auto-triggering modals
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get("action") === "create") {
      handleOpenCreateModal();
    }
  }, [location]);

  const isOfficerOrAdmin = user?.role === "ADMIN" || user?.role === "GOVERNMENT_OFFICER";

  // Fetch scholarships from REST API
  const fetchScholarships = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 6,
        sortBy: "name",
        direction: "asc",
        lang: localStorage.getItem("selectedLanguage") || "en"
      };
      if (search) params.search = search;
      if (educationLevel) params.educationLevel = educationLevel;
      if (category) params.category = category;
      if (income) params.income = parseFloat(income);
      if (stateFilter) params.state = stateFilter;

      const response = await api.get("/scholarships", { params });
      const pageData = response.data.data;
      setScholarships(pageData.content);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.totalElements);
    } catch (e) {
      console.error("Failed to load scholarships", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchScholarships();
  }, [page, educationLevel, category, i18n.language]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchScholarships();
  };

  const handleResetFilters = () => {
    setSearch("");
    setEducationLevel("");
    setCategory("");
    setIncome("");
    setStateFilter("");
    setPage(0);
    setTimeout(() => {
      fetchScholarships();
    }, 50);
  };

  // CRUD Actions
  const handleOpenCreateModal = () => {
    setEditMode(false);
    setFormId(null);
    setFormName("");
    setFormCategory("");
    setFormEducationLevel("");
    setFormIncomeLimit("");
    setFormState("ALL");
    setFormEligibility("");
    setFormAmount("");
    setFormDeadline("");
    setFormDescription("");
    setFormOfficialWebsite("");
    setFormApplyLink("");
    setShowFormModal(true);
  };

  const handleOpenEditModal = (scholarship) => {
    setEditMode(true);
    setFormId(scholarship.id);
    setFormName(scholarship.name);
    setFormCategory(scholarship.category);
    setFormEducationLevel(scholarship.educationLevel);
    setFormIncomeLimit(scholarship.incomeLimit !== null ? scholarship.incomeLimit : "");
    setFormState(scholarship.state || "ALL");
    setFormEligibility(scholarship.eligibility || "");
    setFormAmount(scholarship.amount !== null ? scholarship.amount : "");
    setFormDeadline(scholarship.deadline || "");
    setFormDescription(scholarship.description || "");
    setFormOfficialWebsite(scholarship.officialWebsite || "");
    setFormApplyLink(scholarship.applyLink || "");
    setShowFormModal(true);
  };

  const handleDeleteScholarship = async (id) => {
    if (!confirm(t("scholarships.delete_confirm"))) return;
    try {
      await api.delete(`/scholarships/${id}`);
      fetchScholarships();
    } catch (e) {
      console.error("Failed to delete scholarship", e);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);

    const payload = {
      name: formName,
      category: formCategory,
      educationLevel: formEducationLevel,
      incomeLimit: formIncomeLimit ? parseFloat(formIncomeLimit) : null,
      state: formState,
      eligibility: formEligibility || null,
      amount: formAmount ? parseFloat(formAmount) : null,
      deadline: formDeadline || null,
      description: formDescription || null,
      officialWebsite: formOfficialWebsite || null,
      applyLink: formApplyLink || null,
    };

    try {
      if (editMode) {
        await api.put(`/scholarships/${formId}`, payload);
        setAdminSuccessMsg("Scholarship program updated successfully!");
      } else {
        await api.post("/scholarships", payload);
        setAdminSuccessMsg("Scholarship program published successfully! Email notifications are being dispatched to all registered users.");
      }
      setShowFormModal(false);
      fetchScholarships();
      setTimeout(() => setAdminSuccessMsg(""), 6000);
    } catch (err) {
      alert("Submission failed: " + (err.response?.data?.message || "Verify your inputs."));
    } finally {
      setFormLoading(false);
    }
  };

  const isRecent = (dateStr) => {
    if (!dateStr) return false;
    const createdDate = new Date(dateStr);
    const diffTime = Math.abs(new Date() - createdDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7;
  };

  const educationLevels = [
    "High School",
    "Secondary",
    "Undergraduate",
    "Postgraduate",
    "PhD",
  ];

  const categories = [
    "Social Welfare",
    "Merit-based",
    "Academic Excellence",
    "Minority",
    "Disabled",
  ];

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col">
      <Navbar />

      <main className="flex-grow px-6 md:px-12 py-10 max-w-6xl mx-auto w-full">
        {/* Header */}
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 mb-10">
          <div className="space-y-2">
            <h2 className="text-3xl font-extrabold text-white flex items-center gap-2">
              <BookOpen className="h-8 w-8 text-brand-500" />
              {t("common.scholarships")}
            </h2>
            <p className="text-slate-400 text-xs">
              {t("scholarships.subtitle")}
            </p>
          </div>

          {isOfficerOrAdmin && (
            <button
              onClick={handleOpenCreateModal}
              className="btn-glass-primary flex items-center gap-1.5 py-2.5 px-5 rounded-xl text-xs font-semibold cursor-pointer"
            >
              <Plus className="h-4.5 w-4.5" /> {t("scholarships.publish_new")}
            </button>
          )}
        </div>

        {adminSuccessMsg && (
          <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-4 mb-6 flex items-center gap-3 text-emerald-400 text-xs text-left">
            <CheckCircle2 className="h-5 w-5 flex-shrink-0" />
            <span>{adminSuccessMsg}</span>
          </div>
        )}

        {/* Filters */}
        <form onSubmit={handleSearchSubmit} className="glass-panel p-6 rounded-2xl mb-8 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="md:col-span-2 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder={t("scholarships.search_placeholder")}
                className="w-full glass-input pl-9 text-xs py-2.5"
              />
            </div>

            <div>
              <select
                value={educationLevel}
                onChange={(e) => setEducationLevel(e.target.value)}
                className="w-full glass-input text-xs py-2.5 bg-slate-900 border-white/10"
              >
                <option value="">{t("scholarships.all_levels")}</option>
                {educationLevels.map((lvl) => (
                  <option key={lvl} value={lvl}>{lvl}</option>
                ))}
              </select>
            </div>

            <div>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full glass-input text-xs py-2.5 bg-slate-900 border-white/10"
              >
                <option value="">{t("schemes.all_categories")}</option>
                {categories.map((cat) => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 pt-2 border-t border-white/5">
            <div>
              <input
                type="text"
                value={stateFilter}
                onChange={(e) => setStateFilter(e.target.value)}
                placeholder={t("scholarships.state_domicile")}
                className="w-full glass-input text-xs py-2"
              />
            </div>
            <div>
              <input
                type="number"
                value={income}
                onChange={(e) => setIncome(e.target.value)}
                placeholder={t("scholarships.max_income")}
                className="w-full glass-input text-xs py-2"
              />
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                className="btn-glass-primary px-5 py-2 rounded-lg text-xs font-semibold flex-1 cursor-pointer"
              >
                {t("scholarships.filter_grants")}
              </button>
              <button
                type="button"
                onClick={handleResetFilters}
                className="btn-glass-secondary px-3 py-2 rounded-lg text-xs"
              >
                {t("scholarships.reset")}
              </button>
            </div>
          </div>
        </form>

        {/* Results grid */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="h-10 w-10 text-brand-500 animate-spin" />
          </div>
        ) : scholarships.length === 0 ? (
          <div className="glass-panel p-16 rounded-2xl text-center space-y-4">
            <BookOpen className="h-12 w-12 text-slate-600 mx-auto" />
            <h3 className="text-xl font-bold text-white">{t("scholarships.no_scholarships_found")}</h3>
            <p className="text-slate-400 text-xs max-w-sm mx-auto">
              {t("scholarships.no_scholarships_desc")}
            </p>
            <button
              onClick={handleResetFilters}
              className="btn-glass-secondary py-2 px-4 rounded-xl text-xs font-semibold"
            >
              {t("schemes.reset_all")}
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-left">
              {scholarships.map((scholarship) => (
                <div key={scholarship.id} className="glass-panel p-6 rounded-2xl flex flex-col justify-between border border-white/5 relative">
                  {isOfficerOrAdmin && (
                    <div className="absolute top-4 right-4 flex items-center gap-1.5">
                      <button
                        onClick={() => handleOpenEditModal(scholarship)}
                        className="p-1.5 rounded bg-white/5 border border-white/10 text-slate-400 hover:text-brand-500 transition-colors cursor-pointer"
                        title={t("scholarships.edit_scholarship")}
                      >
                        <Edit className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteScholarship(scholarship.id)}
                        className="p-1.5 rounded bg-white/5 border border-white/10 text-slate-400 hover:text-rose-400 transition-colors cursor-pointer"
                        title={t("scholarships.delete_scholarship")}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  )}

                  <div className="space-y-4">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="inline-block px-2.5 py-0.5 rounded-full text-[10px] bg-brand-500/10 border border-brand-500/25 text-brand-300 font-semibold">
                        {scholarship.category}
                      </span>
                      <span className="inline-block px-2.5 py-0.5 rounded-full text-[10px] bg-accent-teal/10 border border-accent-teal/20 text-teal-300 font-semibold">
                        {scholarship.educationLevel}
                      </span>
                      {isRecent(scholarship.createdAt) && (
                        <span className="inline-block px-2 py-0.5 rounded-full text-[9px] bg-emerald-500/10 border border-emerald-500/20 text-emerald-400 font-bold uppercase tracking-wider">
                          {t("schemes.recent_new")}
                        </span>
                      )}
                    </div>

                    <h3 className="text-lg font-bold text-white leading-snug pr-16">
                      {scholarship.name}
                    </h3>

                    <p className="text-slate-400 text-xs leading-relaxed line-clamp-3">
                      {scholarship.description}
                    </p>

                    <div className="grid grid-cols-2 gap-3 text-[11px] border-t border-white/5 pt-3">
                      <div>
                        <span className="text-slate-500 block">{t("scholarships.amount")}</span>
                        <span className="text-brand-300 font-extrabold text-sm">
                          {scholarship.amount ? `₹${scholarship.amount.toLocaleString()}` : t("scholarships.variable")}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("schemes.deadline")}</span>
                        <span className="text-rose-400 font-semibold flex items-center gap-1">
                          <Calendar className="h-3 w-3" />
                          {scholarship.deadline ? new Date(scholarship.deadline).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' }) : t("schemes.no_deadline")}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("scholarships.state_domicile")}</span>
                        <span className="text-slate-300 font-semibold">{scholarship.state || "ALL"}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("schemes.income_limit")}</span>
                        <span className="text-slate-300 font-semibold">
                          {scholarship.incomeLimit ? `Under ₹${scholarship.incomeLimit.toLocaleString()}` : t("schemes.no_limit")}
                        </span>
                      </div>
                    </div>

                    {scholarship.eligibility && (
                      <div className="bg-white/5 rounded-lg p-3 text-[10px] text-slate-300">
                        <strong className="text-white">{t("schemes.eligibility")}:</strong> {scholarship.eligibility}
                      </div>
                    )}
                  </div>

                  <div className="flex gap-3 border-t border-white/5 pt-4 mt-6">
                    {scholarship.officialWebsite && (
                      <a
                        href={scholarship.officialWebsite}
                        target="_blank"
                        rel="noreferrer"
                        className="btn-glass-secondary flex-1 flex items-center justify-center gap-1 py-2 rounded-lg text-xs"
                      >
                        <Globe className="h-3.5 w-3.5" /> {t("common.visit_website")}
                      </a>
                    )}
                    {scholarship.applyLink && (
                      <a
                        href={scholarship.applyLink}
                        target="_blank"
                        rel="noreferrer"
                        className="btn-glass-primary flex-1 flex items-center justify-center gap-1 py-2 rounded-lg text-xs font-semibold"
                      >
                        <FileText className="h-3.5 w-3.5" /> {t("common.apply_now")}
                      </a>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-white/5 pt-6 mt-8">
                <span className="text-xs text-slate-400">
                  {t("scholarships.showing_page", { page: page + 1, totalPages, totalElements })}
                </span>
                <div className="flex gap-2">
                  <button
                    onClick={() => setPage((p) => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="p-2 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 disabled:opacity-50 text-white cursor-pointer"
                  >
                    <ChevronLeft className="h-4 w-4" />
                  </button>
                  <button
                    onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                    disabled={page === totalPages - 1}
                    className="p-2 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 disabled:opacity-50 text-white cursor-pointer"
                  >
                    <ChevronRight className="h-4 w-4" />
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </main>

      {/* CRUD FORM MODAL */}
      {showFormModal && (
        <div className="fixed inset-0 z-50 bg-[#000]/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="w-full max-w-2xl glass-panel p-6 rounded-2xl relative my-8 animate-slide-up max-h-[90vh] overflow-y-auto">
            <button
              onClick={() => setShowFormModal(false)}
              className="absolute top-4 right-4 p-1.5 rounded-lg bg-white/5 border border-white/10 text-slate-400 hover:text-white cursor-pointer"
            >
              <X className="h-4.5 w-4.5" />
            </button>

            <h3 className="text-xl font-bold text-white mb-6">
              {editMode ? t("scholarships.edit_scholarship_title") : t("scholarships.publish_scholarship_title")}
            </h3>

            <form onSubmit={handleFormSubmit} className="space-y-4 text-left">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("scholarships.scholarship_name")}</label>
                  <input
                    type="text"
                    required
                    value={formName}
                    onChange={(e) => setFormName(e.target.value)}
                    placeholder="e.g. Post-Matric SC/ST Scholarship"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("scholarships.category")}</label>
                  <select
                    required
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10"
                  >
                    <option value="">{t("scholarships.select_category")}</option>
                    {categories.map((cat) => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("scholarships.education_level")}</label>
                  <select
                    required
                    value={formEducationLevel}
                    onChange={(e) => setFormEducationLevel(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10"
                  >
                    <option value="">{t("scholarships.select_level")}</option>
                    {educationLevels.map((lvl) => (
                      <option key={lvl} value={lvl}>{lvl}</option>
                    ))}
                  </select>
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("scholarships.max_income_limit")}</label>
                  <input
                    type="number"
                    value={formIncomeLimit}
                    onChange={(e) => setFormIncomeLimit(e.target.value)}
                    placeholder="e.g. 250000"
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.state_covered")}</label>
                  <input
                    type="text"
                    value={formState}
                    onChange={(e) => setFormState(e.target.value)}
                    placeholder="ALL or specific state"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("scholarships.amount")}</label>
                  <input
                    type="number"
                    value={formAmount}
                    onChange={(e) => setFormAmount(e.target.value)}
                    placeholder="e.g. 15000"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.app_deadline")}</label>
                  <input
                    type="date"
                    value={formDeadline}
                    onChange={(e) => setFormDeadline(e.target.value)}
                    className="w-full glass-input text-xs text-slate-300"
                  />
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-300">{t("schemes.description")}</label>
                <textarea
                  rows="3"
                  value={formDescription}
                  onChange={(e) => setFormDescription(e.target.value)}
                  placeholder="Overview of the scholarship parameters..."
                  className="w-full glass-input text-xs"
                />
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-300">{t("scholarships.eligibility_details")}</label>
                <textarea
                  rows="2"
                  value={formEligibility}
                  onChange={(e) => setFormEligibility(e.target.value)}
                  placeholder="e.g. Secured 55% in class 8, studying in govt schools..."
                  className="w-full glass-input text-xs"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.official_website_url")}</label>
                  <input
                    type="url"
                    value={formOfficialWebsite}
                    onChange={(e) => setFormOfficialWebsite(e.target.value)}
                    placeholder="https://scholarships.gov.in"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.direct_apply_url")}</label>
                  <input
                    type="url"
                    value={formApplyLink}
                    onChange={(e) => setFormApplyLink(e.target.value)}
                    placeholder="https://scholarships.gov.in/apply"
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-6 border-t border-white/5">
                <button
                  type="button"
                  onClick={() => setShowFormModal(false)}
                  className="btn-glass-secondary py-2.5 px-5 rounded-xl text-xs font-semibold"
                >
                  {t("common.cancel")}
                </button>
                <button
                  type="submit"
                  disabled={formLoading}
                  className="btn-glass-primary flex items-center justify-center gap-1.5 py-2.5 px-6 rounded-xl text-xs font-semibold disabled:opacity-50 cursor-pointer"
                >
                  {formLoading ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" /> {t("scholarships.saving")}
                    </>
                  ) : (
                    t("scholarships.publish_new")
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <footer className="border-t border-white/5 py-8 text-center text-xs text-slate-500">
        <p>&copy; {new Date().getFullYear()} {t("schemes.db_info")}</p>
      </footer>
    </div>
  );
};

export default ScholarshipsPage;
