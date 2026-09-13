const fs = require('fs');

const BASE_URL = "http://localhost:8080/api";

async function inspectCache() {
  const timestamp = Date.now();
  const citizenEmail = `cache_builder_${timestamp}@janaai.org`;
  const citizenPassword = "SecurePassword123!";

  async function apiCall(path, options = {}) {
    const url = `${BASE_URL}${path}`;
    const headers = {
      "Content-Type": "application/json",
      ...options.headers,
    };
    const response = await fetch(url, { ...options, headers });
    return { status: response.status, data: await response.json() };
  }

  // 1. Citizen Registration
  const regRes = await apiCall("/auth/register", {
    method: "POST",
    body: JSON.stringify({
      fullName: "Cache Inspector",
      email: citizenEmail,
      password: citizenPassword,
      role: "CITIZEN",
      preferredLanguage: "en",
    }),
  });

  // 2. Citizen Login
  const loginRes = await apiCall("/auth/login", {
    method: "POST",
    body: JSON.stringify({
      email: citizenEmail,
      password: citizenPassword,
    }),
  });

  const token = loginRes.data.data.accessToken;

  // Fetch all schemes
  const schemesRes = await apiCall("/schemes?page=0&size=100", {
    headers: { Authorization: `Bearer ${token}` }
  });
  const schemes = schemesRes.data.data.content;

  // Fetch all scholarships
  const scholarshipsRes = await apiCall("/scholarships?page=0&size=100", {
    headers: { Authorization: `Bearer ${token}` }
  });
  const scholarships = scholarshipsRes.data.data.content;

  // Load existing cache
  const cachePath = "src/main/resources/translations_cache.json";
  let cache = {};
  if (fs.existsSync(cachePath)) {
    cache = JSON.parse(fs.readFileSync(cachePath, 'utf8'));
  }

  const fieldsToTranslate = new Set();
  const targetLanguages = ['kn', 'hi'];

  function check(text) {
    if (!text || text.trim() === '') return;
    const trimmed = text.trim();
    targetLanguages.forEach(lang => {
      const key = `${lang}:${trimmed}`;
      if (!cache[key]) {
        fieldsToTranslate.add(trimmed);
      }
    });
  }

  // Inspect Schemes
  schemes.forEach(s => {
    check(s.schemeName);
    check(s.description);
    check(s.category);
    check(s.state);
    check(s.eligibility);
    check(s.education);
    check(s.benefits);
    check(s.requiredDocuments);
  });

  // Inspect Scholarships
  scholarships.forEach(s => {
    check(s.name);
    check(s.description);
    check(s.category);
    check(s.eligibility);
    check(s.educationLevel);
    check(s.state);
  });

  console.log(`Found ${fieldsToTranslate.size} unique untranslated English strings.`);
  console.log(JSON.stringify(Array.from(fieldsToTranslate), null, 2));
}

inspectCache();
