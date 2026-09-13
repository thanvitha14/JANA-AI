-- Migration to update broken, outdated, or misconfigured scheme and scholarship links
-- 1. Rythu Bandhu Scheme (Telangana) -> Rythu Bharosa
UPDATE government_schemes 
SET official_website = 'https://rythubharosa.telangana.gov.in',
    apply_link = 'https://rythubharosa.telangana.gov.in'
WHERE id = 'sch-048';

-- 2. Kerala E-Grantz (Removing www prefix to match SSL certificate correctly)
UPDATE scholarships
SET official_website = 'https://egrantz.kerala.gov.in',
    apply_link = 'https://egrantz.kerala.gov.in'
WHERE id IN ('scholar-038', 'scholar-039');

-- 3. KVPY (Discontinued/merged -> Redirecting to DST INSPIRE portal)
UPDATE scholarships
SET official_website = 'https://online-inspire.gov.in',
    apply_link = 'https://online-inspire.gov.in'
WHERE id = 'scholar-017';

-- 4. MP Post Matric Scholarship (Outdated nic.in domain -> mp.gov.in domain)
UPDATE scholarships
SET official_website = 'https://www.scholarshipportal.mp.gov.in',
    apply_link = 'https://www.scholarshipportal.mp.gov.in'
WHERE id = 'scholar-027';

-- 5. Haryana Post Matric Scholarship (Adding fallback portal domain)
UPDATE scholarships
SET official_website = 'https://highereduhry.ac.in',
    apply_link = 'https://harchhatrabratti.highereduhry.ac.in'
WHERE id = 'scholar-035';


