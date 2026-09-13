-- Migration to update remaining broken or timed out links to stable top-level domains
-- 1. MP Post Matric Scholarship (Reliable MP Government State Portal)
UPDATE scholarships
SET official_website = 'https://mp.gov.in',
    apply_link = 'https://mp.gov.in'
WHERE id = 'scholar-027';

-- 2. Haryana Post Matric Scholarship (Reliable Haryana Government State Portal)
UPDATE scholarships
SET official_website = 'https://haryana.gov.in',
    apply_link = 'https://haryana.gov.in'
WHERE id = 'scholar-035';

-- 3. Bihar Post Matric Scholarship (Reliable Bihar Government State Portal)
UPDATE scholarships
SET official_website = 'https://state.bihar.gov.in',
    apply_link = 'https://state.bihar.gov.in'
WHERE id IN ('scholar-033', 'scholar-034');

-- 4. Ladki Bahin Scheme (Maharashtra) (Reliable Maharashtra State Portal)
UPDATE government_schemes
SET official_website = 'https://maharashtra.gov.in',
    apply_link = 'https://maharashtra.gov.in'
WHERE id = 'sch-060';
