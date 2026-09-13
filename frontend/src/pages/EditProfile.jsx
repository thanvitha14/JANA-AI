import React, { useState, useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import api from "../services/api";
import Navbar from "../components/Navbar";
import { User, Mail, Phone, Calendar, Globe, MapPin, Loader2, Save, CheckCircle2, AlertCircle, Camera } from "lucide-react";
import { useTranslation } from "react-i18next";

const EditProfile = () => {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const fileInputRef = useRef(null);

  // Form states
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState(""); // Disabled/Read-only
  const [phoneNumber, setPhoneNumber] = useState("");
  const [dateOfBirth, setDateOfBirth] = useState("");
  const [gender, setGender] = useState("");
  const [city, setCity] = useState("");
  const [state, setState] = useState("");
  const [preferredLanguage, setPreferredLanguage] = useState("en");
  const [profilePhotoUrl, setProfilePhotoUrl] = useState("");

  // UI status states
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [photoUploading, setPhotoUploading] = useState(false);
  const [successMsg, setSuccessMsg] = useState("");
  const [errorMsg, setErrorMsg] = useState("");

  const getPhotoUrl = (path) => {
    if (!path) return null;
    if (path.startsWith("http://") || path.startsWith("https://")) return path;
    const baseUrl = api?.defaults?.baseURL || "http://localhost:8080/api";
    const host = baseUrl.replace("/api", "");
    return `${host}${path}`;
  };

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setErrorMsg("");
      try {
        const response = await api.get("/users/profile");
        const profile = response.data.data;
        
        setFullName(profile.fullName || "");
        setEmail(profile.email || "");
        setPhoneNumber(profile.phoneNumber || "");
        setDateOfBirth(profile.dateOfBirth || "");
        setGender(profile.gender || "");
        setCity(profile.city || "");
        setState(profile.state || "");
        setPreferredLanguage(profile.preferredLanguage || "en");
        setProfilePhotoUrl(profile.profilePhotoUrl || "");
      } catch (err) {
        console.error("Failed to load user profile", err);
        setErrorMsg("Failed to retrieve profile data. Please refresh or try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, []);

  const handleUploadClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    // Validate size (5MB max)
    if (file.size > 5 * 1024 * 1024) {
      setErrorMsg("File size exceeds the maximum limit of 5MB.");
      return;
    }

    // Validate format
    const allowedTypes = ["image/jpeg", "image/jpg", "image/png", "image/webp"];
    if (!allowedTypes.includes(file.type)) {
      setErrorMsg("Only JPG, JPEG, PNG, and WEBP image formats are supported.");
      return;
    }

    setPhotoUploading(true);
    setErrorMsg("");
    setSuccessMsg("");

    const formData = new FormData();
    formData.append("file", file);

    try {
      const res = await api.post("/users/profile/photo", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      const updatedUser = res.data.data;
      
      setProfilePhotoUrl(updatedUser.profilePhotoUrl || "");
      
      // Update global context so header immediately refreshes
      updateUser({
        profilePhotoUrl: updatedUser.profilePhotoUrl,
      });

      setSuccessMsg("Profile picture updated successfully!");
      setTimeout(() => setSuccessMsg(""), 4000);
    } catch (err) {
      console.error("Failed to upload profile picture", err);
      setErrorMsg(err.response?.data?.message || "Failed to upload photo. Please try again.");
    } finally {
      setPhotoUploading(false);
      // Reset input value so same file can be selected again
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setErrorMsg("");
    setSuccessMsg("");

    const payload = {
      fullName,
      phoneNumber: phoneNumber || null,
      dateOfBirth: dateOfBirth || null,
      gender: gender || null,
      city: city || null,
      state: state || null,
      preferredLanguage,
    };

    try {
      const response = await api.put("/users/profile", payload);
      const updatedUser = response.data.data;
      
      // Update AuthContext user info
      updateUser({
        fullName: updatedUser.fullName,
      });

      setSuccessMsg("Your profile details have been saved successfully!");
      setTimeout(() => setSuccessMsg(""), 4000);
    } catch (err) {
      console.error("Failed to update profile", err);
      setErrorMsg(err.response?.data?.message || "Profile update failed. Please check your inputs.");
    } finally {
      setSaving(false);
    }
  };

  const languages = [
    { code: "en", name: "English" },
    { code: "hi", name: "Hindi" },
    { code: "ta", name: "Tamil" },
    { code: "te", name: "Telugu" },
    { code: "mr", name: "Marathi" },
    { code: "bn", name: "Bengali" },
  ];

  const genders = ["MALE", "FEMALE", "OTHER", "PREFER_NOT_TO_SAY"];

  return (
    <div className="min-h-screen bg-[#0b0c10] text-slate-100 flex flex-col">
      <Navbar />

      <main className="flex-grow px-6 md:px-12 py-10 max-w-3xl mx-auto w-full relative">
        {/* Background Decorative Glow */}
        <div className="absolute top-1/3 left-1/2 -translate-x-1/2 -translate-y-1/2 w-80 h-80 rounded-full bg-brand-500/5 blur-[100px] -z-10"></div>

        {/* Header */}
        <div className="space-y-2 mb-8 text-left">
          <h2 className="text-3xl font-extrabold text-white flex items-center gap-2">
            <User className="h-8 w-8 text-brand-500" />
            {t("profile.title")}
          </h2>
          <p className="text-slate-400 text-xs">
            {t("profile.subtitle")}
          </p>
        </div>

        {/* Loading Spinner */}
        {loading ? (
          <div className="flex justify-center items-center py-24 glass-panel rounded-2xl border border-white/5">
            <Loader2 className="h-10 w-10 text-brand-500 animate-spin" />
          </div>
        ) : (
          <div className="glass-panel p-8 rounded-2xl border border-white/5 text-left">
            {/* Status Messages */}
            {successMsg && (
              <div className="bg-emerald-500/10 border border-emerald-500/30 rounded-xl p-4 mb-6 flex items-center gap-3 text-emerald-400 text-xs">
                <CheckCircle2 className="h-5 w-5 flex-shrink-0" />
                <span>{t("profile.update_success")}</span>
              </div>
            )}

            {errorMsg && (
              <div className="bg-rose-500/10 border border-rose-500/30 rounded-xl p-4 mb-6 flex items-center gap-3 text-rose-400 text-xs">
                <AlertCircle className="h-5 w-5 flex-shrink-0" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Profile Photo Upload / Preview */}
              <div className="flex flex-col sm:flex-row items-center gap-6 pb-6 border-b border-white/5 text-left">
                {profilePhotoUrl ? (
                  <div className="relative group">
                    <img
                      src={getPhotoUrl(profilePhotoUrl)}
                      className="h-24 w-24 rounded-full object-cover border-2 border-brand-500/30 transition-all duration-200 group-hover:opacity-80"
                      alt={t("profile.avatar_alt")}
                    />
                    <button
                      type="button"
                      onClick={handleUploadClick}
                      className="absolute bottom-0 right-0 p-1.5 bg-brand-500 text-white rounded-full border border-slate-900 shadow-lg hover:bg-brand-600 transition-colors"
                      title={t("profile.change_avatar")}
                    >
                      <Camera className="h-4 w-4" />
                    </button>
                  </div>
                ) : (
                  <div className="relative">
                    <div className="h-24 w-24 rounded-full bg-brand-500/10 border border-brand-500/20 flex items-center justify-center text-brand-500 font-extrabold text-4xl uppercase">
                      {fullName ? fullName.charAt(0) : "?"}
                    </div>
                    <button
                      type="button"
                      onClick={handleUploadClick}
                      className="absolute bottom-0 right-0 p-1.5 bg-brand-500 text-white rounded-full border border-slate-900 shadow-lg hover:bg-brand-600 transition-colors"
                      title={t("profile.upload_avatar")}
                    >
                      <Camera className="h-4 w-4" />
                    </button>
                  </div>
                )}

                <div className="space-y-2 text-center sm:text-left">
                  <label className="text-xs font-semibold text-slate-300 block">{t("profile.profile_picture")}</label>
                  <button
                    type="button"
                    onClick={handleUploadClick}
                    disabled={photoUploading}
                    className="btn-glass-primary py-1.5 px-4 rounded-xl text-[11px] font-semibold cursor-pointer inline-flex items-center gap-1.5"
                  >
                    {photoUploading ? (
                      <>
                        <Loader2 className="h-3.5 w-3.5 animate-spin" /> {t("profile.uploading")}
                      </>
                    ) : (
                      t("profile.change_photo")
                    )}
                  </button>
                  <span className="text-[10px] text-slate-500 block">
                    {t("profile.accepts_formats")}
                  </span>
                  <input
                    type="file"
                    ref={fileInputRef}
                    onChange={handleFileChange}
                    accept="image/jpeg,image/jpg,image/png,image/webp"
                    className="hidden"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* Full Name */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <User className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.full_name")}
                  </label>
                  <input
                    type="text"
                    required
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    placeholder={t("profile.full_name")}
                    className="w-full glass-input text-xs"
                  />
                </div>

                {/* Email (Disabled) */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <Mail className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.email_readonly")}
                  </label>
                  <input
                    type="email"
                    disabled
                    value={email}
                    className="w-full glass-input text-xs opacity-60 bg-white/5 border-white/5 cursor-not-allowed"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* Phone Number */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <Phone className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.phone_number")}
                  </label>
                  <input
                    type="tel"
                    value={phoneNumber}
                    onChange={(e) => setPhoneNumber(e.target.value)}
                    placeholder={t("profile.phone_number")}
                    className="w-full glass-input text-xs"
                  />
                </div>

                {/* Date of Birth */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <Calendar className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.dob")}
                  </label>
                  <input
                    type="date"
                    value={dateOfBirth}
                    onChange={(e) => setDateOfBirth(e.target.value)}
                    className="w-full glass-input text-xs text-slate-300"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* Gender */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <User className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.gender")}
                  </label>
                  <select
                    value={gender}
                    onChange={(e) => setGender(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10 capitalize"
                  >
                    <option value="">{t("profile.select_gender")}</option>
                    {genders.map((g) => (
                      <option key={g} value={g}>
                        {g.toLowerCase().replace(/_/g, " ")}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Preferred Language */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <Globe className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.pref_lang")}
                  </label>
                  <select
                    value={preferredLanguage}
                    onChange={(e) => setPreferredLanguage(e.target.value)}
                    className="w-full glass-input text-xs bg-slate-900 border-white/10"
                  >
                    {languages.map((l) => (
                      <option key={l.code} value={l.code}>
                        {l.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                {/* City */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.city")}
                  </label>
                  <input
                    type="text"
                    value={city}
                    onChange={(e) => setCity(e.target.value)}
                    placeholder={t("profile.city")}
                    className="w-full glass-input text-xs"
                  />
                </div>

                {/* State */}
                <div className="space-y-1.5">
                  <label className="text-xs font-semibold text-slate-300 flex items-center gap-1.5">
                    <MapPin className="h-3.5 w-3.5 text-slate-400" />
                    {t("profile.state")}
                  </label>
                  <input
                    type="text"
                    value={state}
                    onChange={(e) => setState(e.target.value)}
                    placeholder={t("profile.state")}
                    className="w-full glass-input text-xs"
                  />
                </div>
              </div>

              {/* Form buttons */}
              <div className="flex justify-end gap-3 pt-6 border-t border-white/5">
                <button
                  type="button"
                  onClick={() => navigate("/dashboard")}
                  className="btn-glass-secondary py-2.5 px-5 rounded-xl text-xs font-semibold"
                >
                  {t("profile.cancel")}
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="btn-glass-primary flex items-center justify-center gap-1.5 py-2.5 px-6 rounded-xl text-xs font-semibold disabled:opacity-50 cursor-pointer"
                >
                  {saving ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" /> {t("profile.saving")}
                    </>
                  ) : (
                    <>
                      <Save className="h-4 w-4" /> {t("profile.save_profile")}
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        )}
      </main>

      <footer className="border-t border-white/5 py-6 text-center text-xs text-slate-500">
        <p>&copy; {new Date().getFullYear()} {t("profile.session_info")}</p>
      </footer>
    </div>
  );
};

export default EditProfile;
