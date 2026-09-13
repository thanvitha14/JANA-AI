-- Update Apply links to use top-level working portals to avoid broken redirects
UPDATE government_schemes 
SET apply_link = 'https://pmjdy.gov.in' 
WHERE id = 'd508ad00-a292-4f7f-8547-817a151b75a1';

UPDATE government_schemes 
SET apply_link = 'https://pmjay.gov.in' 
WHERE id = 'd508ad00-a292-4f7f-8547-817a151b75a2';

UPDATE government_schemes 
SET apply_link = 'https://pmkisan.gov.in' 
WHERE id = 'd508ad00-a292-4f7f-8547-817a151b75a3';

UPDATE government_schemes 
SET apply_link = 'https://sanjeevani.rajasthan.gov.in' 
WHERE id = 'd508ad00-a292-4f7f-8547-817a151b75a4';

UPDATE scholarships 
SET apply_link = 'https://scholarships.gov.in' 
WHERE id IN ('f82b8a00-b531-4e8c-9642-127b151b75a1', 'f82b8a00-b531-4e8c-9642-127b151b75a2', 'f82b8a00-b531-4e8c-9642-127b151b75a3');
