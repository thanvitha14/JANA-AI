import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import { Award, Search, Filter, Plus, Edit, Trash2, Globe, FileText, ChevronLeft, ChevronRight, X, Loader2, DollarSign, UserCheck, CheckCircle2, Calendar } from "lucide-react";
import Navbar from "../components/Navbar";
import { useLocation } from "react-router-dom";
import { useTranslation } from "react-i18next";

const SchemesPage = () => {
  const { user } = useAuth();
  const location = useLocation();
  const { t, i18n } = useTranslation();

  // Core Schemes State
  const [schemes, setSchemes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // Filters State
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");
  const [stateFilter, setStateFilter] = useState("");
  const [gender, setGender] = useState("");
  const [income, setIncome] = useState("");
  const [age, setAge] = useState("");
  const [education, setEducation] = useState("");

  // CRUD Form Dialog State
  const [showFormModal, setShowFormModal] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [editMode, setEditMode] = useState(false);
  const [formId, setFormId] = useState(null);
  
  const [formSchemeName, setFormSchemeName] = useState("");
  const [formCategory, setFormCategory] = useState("");
  const [formDescription, setFormDescription] = useState("");
  const [formState, setFormState] = useState("ALL");
  const [formEligibility, setFormEligibility] = useState("");
  const [formIncomeLimit, setFormIncomeLimit] = useState("");
  const [formGender, setFormGender] = useState("ALL");
  const [formMinimumAge, setFormMinimumAge] = useState("");
  const [formMaximumAge, setFormMaximumAge] = useState("");
  const [formEducation, setFormEducation] = useState("ALL");
  const [formBenefits, setFormBenefits] = useState("");
  const [formRequiredDocuments, setFormRequiredDocuments] = useState("");
  const [formOfficialWebsite, setFormOfficialWebsite] = useState("");
  const [formApplyLink, setFormApplyLink] = useState("");
  const [formActive, setFormActive] = useState(true);
  const [formDeadline, setFormDeadline] = useState("");
  const [adminSuccessMsg, setAdminSuccessMsg] = useState("");

  // Check query params on mount for auto-triggering modals
  useEffect(() => {
    const params = new URLSearchParams(location.search);
    if (params.get("action") === "create") {
      handleOpenCreateModal();
    }
  }, [location]);

  const isOfficerOrAdmin = user?.role === "ADMIN" || user?.role === "GOVERNMENT_OFFICER";

  // Fetch schemes from REST API
  const fetchSchemes = async () => {
    setLoading(true);
    try {
      const params = {
        page,
        size: 6,
        sortBy: "schemeName",
        direction: "asc",
        lang: localStorage.getItem("selectedLanguage") || "en"
      };
      if (search) params.search = search;
      if (category) params.category = category;
      if (stateFilter) params.state = stateFilter;
      if (gender) params.gender = gender;
      if (income) params.income = parseFloat(income);
      if (age) params.age = parseInt(age, 10);
      if (education) params.education = education;

      const response = await api.get("/schemes", { params });
      const pageData = response.data.data;
      setSchemes(pageData.content);
      setTotalPages(pageData.totalPages);
      setTotalElements(pageData.totalElements);
    } catch (e) {
      console.error("Failed to load government schemes", e);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSchemes();
  }, [page, category, gender, i18n.language]);

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    setPage(0);
    fetchSchemes();
  };

  const handleResetFilters = () => {
    setSearch("");
    setCategory("");
    setStateFilter("");
    setGender("");
    setIncome("");
    setAge("");
    setEducation("");
    setPage(0);
    // Timeout to let state updates apply
    setTimeout(() => {
      fetchSchemes();
    }, 50);
  };

  // CRUD Actions
  const handleOpenCreateModal = () => {
    setEditMode(false);
    setFormId(null);
    setFormSchemeName("");
    setFormCategory("");
    setFormDescription("");
    setFormState("ALL");
    setFormEligibility("");
    setFormIncomeLimit("");
    setFormGender("ALL");
    setFormMinimumAge("");
    setFormMaximumAge("");
    setFormEducation("ALL");
    setFormBenefits("");
    setFormRequiredDocuments("");
    setFormOfficialWebsite("");
    setFormApplyLink("");
    setFormActive(true);
    setFormDeadline("");
    setShowFormModal(true);
  };

  const handleOpenEditModal = (scheme) => {
    setEditMode(true);
    setFormId(scheme.id);
    setFormSchemeName(scheme.schemeName);
    setFormCategory(scheme.category);
    setFormDescription(scheme.description);
    setFormState(scheme.state || "ALL");
    setFormEligibility(scheme.eligibility || "");
    setFormIncomeLimit(scheme.incomeLimit !== null ? scheme.incomeLimit : "");
    setFormGender(scheme.gender || "ALL");
    setFormMinimumAge(scheme.minimumAge !== null ? scheme.minimumAge : "");
    setFormMaximumAge(scheme.maximumAge !== null ? scheme.maximumAge : "");
    setFormEducation(scheme.education || "ALL");
    setFormBenefits(scheme.benefits || "");
    setFormRequiredDocuments(scheme.requiredDocuments || "");
    setFormOfficialWebsite(scheme.officialWebsite || "");
    setFormApplyLink(scheme.applyLink || "");
    setFormActive(scheme.active);
    setFormDeadline(scheme.deadline || "");
    setShowFormModal(true);
  };

  const handleDeleteScheme = async (id) => {
    if (!confirm(t("schemes.delete_confirm"))) return;
    try {
      await api.delete(`/schemes/${id}`);
      fetchSchemes();
    } catch (e) {
      console.error("Failed to delete scheme", e);
    }
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setFormLoading(true);

    const payload = {
      schemeName: formSchemeName,
      category: formCategory,
      description: formDescription,
      state: formState,
      eligibility: formEligibility || null,
      incomeLimit: formIncomeLimit ? parseFloat(formIncomeLimit) : null,
      gender: formGender,
      minimumAge: formMinimumAge ? parseInt(formMinimumAge, 10) : null,
      maximumAge: formMaximumAge ? parseInt(formMaximumAge, 10) : null,
      education: formEducation,
      benefits: formBenefits || null,
      requiredDocuments: formRequiredDocuments || null,
      officialWebsite: formOfficialWebsite || null,
      applyLink: formApplyLink || null,
      deadline: formDeadline || null,
      active: formActive,
    };

    try {
      if (editMode) {
        await api.put(`/schemes/${formId}`, payload);
        setAdminSuccessMsg("Government Scheme updated successfully!");
      } else {
        await api.post("/schemes", payload);
        setAdminSuccessMsg("Government Scheme published successfully! Email notifications are being dispatched to all registered users.");
      }
      setShowFormModal(false);
      fetchSchemes();
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

  const categories = [
    "Financial Inclusion",
    "Healthcare",
    "Agriculture",
    "Women Empowerment",
    "Education",
    "Social Welfare",
  ];

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col">
      <Navbar />

      <main className="flex-grow px-6 md:px-12 py-10 max-w-6xl mx-auto w-full">
        {/* Header */}
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4 mb-10">
          <div className="space-y-2">
            <h2 className="text-3xl font-extrabold text-white flex items-center gap-2">
              <Award className="h-8 w-8 text-brand-500" />
              {t("common.schemes")}
            </h2>
            <p className="text-slate-400 text-xs">
              {t("schemes.subtitle")}
            </p>
          </div>

          {isOfficerOrAdmin && (
            <button
               onClick={handleOpenCreateModal}
               className="btn-glass-primary flex items-center gap-1.5 py-2.5 px-5 rounded-xl text-xs font-semibold cursor-pointer"
            >
               <Plus className="h-4.5 w-4.5" /> {t("schemes.publish_new")}
            </button>
          )}
        </div>

        {adminSuccessMsg && (
          <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-4 mb-6 flex items-center gap-3 text-emerald-400 text-xs text-left">
            <CheckCircle2 className="h-5 w-5 flex-shrink-0" />
            <span>{adminSuccessMsg}</span>
          </div>
        )}

        {/* Filter bar */}
        <form onSubmit={handleSearchSubmit} className="glass-panel p-6 rounded-2xl mb-8 space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="md:col-span-2 relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder={t("schemes.search_placeholder")}
                className="w-full glass-input pl-9 text-xs py-2.5"
              />
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

            <div>
              <select
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                className="w-full glass-input text-xs py-2.5 bg-slate-900 border-white/10"
              >
                <option value="">{t("schemes.any_gender")}</option>
                <option value="ALL">{t("schemes.all_genders")}</option>
                <option value="MALE">{t("schemes.male_only")}</option>
                <option value="FEMALE">{t("schemes.female_only")}</option>
              </select>
            </div>
          </div>

          {/* Advanced eligibility match controls */}
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-4 pt-2 border-t border-white/5">
            <div>
              <input
                type="text"
                value={stateFilter}
                onChange={(e) => setStateFilter(e.target.value)}
                placeholder={t("schemes.state_placeholder")}
                className="w-full glass-input text-xs py-2"
              />
            </div>
            <div>
              <input
                type="number"
                value={income}
                onChange={(e) => setIncome(e.target.value)}
                placeholder={t("schemes.income_placeholder")}
                className="w-full glass-input text-xs py-2"
              />
            </div>
            <div>
              <input
                type="number"
                value={age}
                onChange={(e) => setAge(e.target.value)}
                placeholder={t("schemes.age_placeholder")}
                className="w-full glass-input text-xs py-2"
              />
            </div>
            <div className="flex gap-2">
              <input
                type="text"
                value={education}
                onChange={(e) => setEducation(e.target.value)}
                placeholder={t("schemes.edu_placeholder")}
                className="w-full glass-input text-xs py-2 flex-1"
              />
              <button
                type="submit"
                className="btn-glass-primary px-4 py-2 rounded-lg text-xs font-semibold cursor-pointer"
              >
                {t("schemes.find")}
              </button>
              <button
                type="button"
                onClick={handleResetFilters}
                className="btn-glass-secondary px-3 py-2 rounded-lg text-xs"
                title={t("schemes.clear_filters")}
              >
                {t("schemes.reset")}
              </button>
            </div>
          </div>
        </form>

        {/* Results grid */}
        {loading ? (
          <div className="flex justify-center items-center py-20">
            <Loader2 className="h-10 w-10 text-brand-500 animate-spin" />
          </div>
        ) : schemes.length === 0 ? (
          <div className="glass-panel p-16 rounded-2xl text-center space-y-4">
            <Award className="h-12 w-12 text-slate-600 mx-auto" />
            <h3 className="text-xl font-bold text-white">{t("schemes.no_schemes")}</h3>
            <p className="text-slate-400 text-xs max-w-sm mx-auto">
              {t("schemes.no_schemes_desc")}
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
              {schemes.map((scheme) => (
                <div key={scheme.id} className="glass-panel p-6 rounded-2xl flex flex-col justify-between border border-white/5 relative">
                  {isOfficerOrAdmin && (
                    <div className="absolute top-4 right-4 flex items-center gap-1.5">
                      <button
                        onClick={() => handleOpenEditModal(scheme)}
                        className="p-1.5 rounded bg-white/5 border border-white/10 text-slate-400 hover:text-brand-500 transition-colors cursor-pointer"
                        title={t("schemes.edit_scheme")}
                      >
                        <Edit className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => handleDeleteScheme(scheme.id)}
                        className="p-1.5 rounded bg-white/5 border border-white/10 text-slate-400 hover:text-rose-400 transition-colors cursor-pointer"
                        title={t("schemes.delete_scheme")}
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  )}

                  <div className="space-y-4">
                    <div>
                      <span className="inline-block px-2.5 py-0.5 rounded-full text-[10px] bg-brand-500/10 border border-brand-500/25 text-brand-300 font-semibold mb-2.5">
                        {scheme.category}
                      </span>
                      {isRecent(scheme.createdAt) && (
                        <span className="inline-block px-2.5 py-0.5 rounded-full text-[9px] bg-emerald-500/10 border border-emerald-500/25 text-emerald-400 font-bold uppercase tracking-wider mb-2.5 ml-2">
                          {t("schemes.recent_new")}
                        </span>
                      )}
                      <h3 className="text-lg font-bold text-white leading-snug pr-16">
                        {scheme.schemeName}
                      </h3>
                    </div>

                    <p className="text-slate-400 text-xs leading-relaxed line-clamp-3">
                      {scheme.description}
                    </p>

                    <div className="grid grid-cols-2 gap-3 text-[11px] border-t border-white/5 pt-3">
                      <div>
                        <span className="text-slate-500 block">{t("schemes.state_covered")}</span>
                        <span className="text-slate-300 font-semibold">{scheme.state || "ALL"}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("schemes.income_limit")}</span>
                        <span className="text-slate-300 font-semibold">
                          {scheme.incomeLimit ? `Under ₹${scheme.incomeLimit.toLocaleString()}` : t("schemes.no_limit")}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("schemes.age_target")}</span>
                        <span className="text-slate-300 font-semibold">
                          {scheme.minimumAge || 0} - {scheme.maximumAge || "100+"} {t("schemes.years")}
                        </span>
                      </div>
                      <div>
                        <span className="text-slate-500 block">{t("schemes.target_gender")}</span>
                        <span className="text-slate-300 font-semibold capitalize">{scheme.gender.toLowerCase()}</span>
                      </div>
                      <div>
                        <span className="text-slate-500 block flex items-center gap-1"><Calendar className="h-3 w-3 text-slate-500" /> {t("schemes.deadline")}</span>
                        <span className="text-rose-400 font-semibold">
                          {scheme.deadline ? new Date(scheme.deadline).toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' }) : t("schemes.no_deadline")}
                        </span>
                      </div>
                    </div>

                    {scheme.eligibility && (
                      <div className="bg-white/5 rounded-lg p-3 text-[10px] text-slate-300">
                        <strong className="text-white">{t("schemes.eligibility")}:</strong> {scheme.eligibility}
                      </div>
                    )}
                    {scheme.benefits && (
                      <div className="bg-brand-500/5 rounded-lg p-3 text-[10px] text-brand-300 mt-2">
                        <strong className="text-white block mb-0.5">{t("schemes.benefits")}:</strong> {scheme.benefits}
                      </div>
                    )}
                    {scheme.requiredDocuments && (
                      <div className="bg-slate-900/60 rounded-lg p-3 text-[10px] text-slate-300 mt-2">
                        <strong className="text-white block mb-0.5">{t("schemes.documents")}:</strong> {scheme.requiredDocuments}
                      </div>
                    )}
                  </div>

                  <div className="flex gap-3 border-t border-white/5 pt-4 mt-6">
                    {scheme.officialWebsite && (
                      <a
                        href={scheme.officialWebsite}
                        target="_blank"
                        rel="noreferrer"
                        className="btn-glass-secondary flex-1 flex items-center justify-center gap-1 py-2 rounded-lg text-xs"
                      >
                        <Globe className="h-3.5 w-3.5" /> {t("common.visit_website")}
                      </a>
                    )}
                    {scheme.applyLink && (
                      <a
                        href={scheme.applyLink}
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

            {/* Pagination Controls */}
            {totalPages > 1 && (
              <div className="flex items-center justify-between border-t border-white/5 pt-6 mt-8">
                <span className="text-xs text-slate-400">
                  {t("schemes.showing_page", { page: page + 1, totalPages, totalElements })}
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

      {/* CRUD DIALOG MODAL */}
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
              {editMode ? t("schemes.edit_scheme") : t("schemes.publish_scheme")}
            </h3>

            <form onSubmit={handleFormSubmit} className="space-y-4 text-left">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.scheme_name")}</label>
                  <input
                    type="text"
                    required
                    value={formSchemeName}
                    onChange={(e) => setFormSchemeName(e.target.value)}
                    placeholder="e.g. Pradhan Mantri Awas Yojana"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.category")}</label>
                  <select
                    required
                    value={formCategory}
                    onChange={(e) => setFormCategory(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10"
                  >
                    <option value="">{t("schemes.select_category")}</option>
                    {categories.map((cat) => (
                      <option key={cat} value={cat}>{cat}</option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="space-y-1.5">
                <label className="text-xs font-semibold text-slate-300">{t("schemes.description")}</label>
                <textarea
                  required
                  rows="3"
                  value={formDescription}
                  onChange={(e) => setFormDescription(e.target.value)}
                  placeholder="Detailed breakdown of what this social program does..."
                  className="w-full glass-input text-xs"
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.covered_state")}</label>
                  <input
                    type="text"
                    value={formState}
                    onChange={(e) => setFormState(e.target.value)}
                    placeholder="ALL or specific state"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.gender_eligibility")}</label>
                  <select
                    value={formGender}
                    onChange={(e) => setFormGender(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10"
                  >
                    <option value="ALL">{t("schemes.all_genders")}</option>
                    <option value="MALE">{t("schemes.male_only")}</option>
                    <option value="FEMALE">{t("schemes.female_only")}</option>
                  </select>
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.max_income_limit")}</label>
                  <input
                    type="number"
                    value={formIncomeLimit}
                    onChange={(e) => setFormIncomeLimit(e.target.value)}
                    placeholder="Leave empty if no limit"
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.minimum_age")}</label>
                  <input
                    type="number"
                    value={formMinimumAge}
                    onChange={(e) => setFormMinimumAge(e.target.value)}
                    placeholder="e.g. 18"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.maximum_age")}</label>
                  <input
                    type="number"
                    value={formMaximumAge}
                    onChange={(e) => setFormMaximumAge(e.target.value)}
                    placeholder="e.g. 60"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.education_target")}</label>
                  <input
                    type="text"
                    value={formEducation}
                    onChange={(e) => setFormEducation(e.target.value)}
                    placeholder="ALL or e.g. Class 10"
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.welfare_benefits")}</label>
                  <textarea
                    rows="2"
                    value={formBenefits}
                    onChange={(e) => setFormBenefits(e.target.value)}
                    placeholder="Financial assistance amounts, health cover specifics..."
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.eligibility_details")}</label>
                  <textarea
                    rows="2"
                    value={formEligibility}
                    onChange={(e) => setFormEligibility(e.target.value)}
                    placeholder="e.g. Residing in slums, farming land less than 2 hectares..."
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="space-y-1.5 sm:col-span-2">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.required_documents")}</label>
                  <input
                    type="text"
                    value={formRequiredDocuments}
                    onChange={(e) => setFormRequiredDocuments(e.target.value)}
                    placeholder="Aadhaar, Income Proof, Caste Certificate..."
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

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.official_website_url")}</label>
                  <input
                    type="url"
                    value={formOfficialWebsite}
                    onChange={(e) => setFormOfficialWebsite(e.target.value)}
                    placeholder="https://pm-scheme.gov.in"
                    className="w-full glass-input text-xs"
                  />
                </div>
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300">{t("schemes.direct_apply_url")}</label>
                  <input
                    type="url"
                    value={formApplyLink}
                    onChange={(e) => setFormApplyLink(e.target.value)}
                    placeholder="https://pm-scheme.gov.in/apply"
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <input
                  id="activeCheck"
                  type="checkbox"
                  checked={formActive}
                  onChange={(e) => setFormActive(e.target.checked)}
                  className="rounded border-white/10 text-brand-500 focus:ring-0 focus:ring-offset-0 bg-slate-900 h-4 w-4"
                />
                <label htmlFor="activeCheck" className="text-xs font-semibold text-slate-300 cursor-pointer">
                  {t("schemes.mark_active")}
                </label>
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
                      <Loader2 className="h-4 w-4 animate-spin" /> {t("schemes.saving")}
                    </>
                  ) : (
                    t("schemes.publish_scheme")
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

export default SchemesPage;
