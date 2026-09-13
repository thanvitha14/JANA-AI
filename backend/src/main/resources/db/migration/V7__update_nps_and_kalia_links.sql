-- Migration to update National Pension Scheme and Kalia Scheme links to stable portals
UPDATE government_schemes
SET official_website = 'https://india.gov.in',
    apply_link = 'https://india.gov.in'
WHERE id = 'sch-029';

UPDATE government_schemes
SET official_website = 'https://odisha.gov.in',
    apply_link = 'https://odisha.gov.in'
WHERE id = 'sch-049';
