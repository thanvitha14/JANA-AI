import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import translationEN from "./locales/en/translation.json";
import translationKN from "./locales/kn/translation.json";
import translationHI from "./locales/hi/translation.json";

const resources = {
  en: {
    translation: translationEN
  },
  kn: {
    translation: translationKN
  },
  hi: {
    translation: translationHI
  }
};

const savedLanguage = localStorage.getItem("selectedLanguage") || "en";

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    lng: savedLanguage,
    fallbackLng: "en",
    interpolation: {
      escapeValue: false
    }
  });

export default i18n;
