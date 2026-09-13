-- Migration to update Kerala scholarship links to the stable top-level Kerala government portal
UPDATE scholarships
SET official_website = 'https://kerala.gov.in',
    apply_link = 'https://kerala.gov.in'
WHERE id IN ('scholar-038', 'scholar-039');
