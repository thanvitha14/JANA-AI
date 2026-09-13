-- Initialize database schema for JANA AI

-- 1. Roles Table
CREATE TABLE roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed Roles
INSERT INTO roles (name, description) VALUES ('CITIZEN', 'General citizen user');
INSERT INTO roles (name, description) VALUES ('NGO', 'Non-Governmental Organization representative');
INSERT INTO roles (name, description) VALUES ('GOVERNMENT_OFFICER', 'Government Officer for scheme management');
INSERT INTO roles (name, description) VALUES ('ADMIN', 'System Administrator');

-- 2. Users Table
CREATE TABLE users (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(20),
    address_line VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    pincode VARCHAR(20),
    latitude DOUBLE,
    longitude DOUBLE,
    preferred_language VARCHAR(10) DEFAULT 'en',
    profile_photo_url VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_count INT NOT NULL DEFAULT 0,
    account_locked_until DATETIME,
    last_login_at DATETIME,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted_at DATETIME,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- 3. Refresh Tokens Table
CREATE TABLE refresh_tokens (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address VARCHAR(45),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- 4. Government Schemes Table
CREATE TABLE government_schemes (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    scheme_name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    state VARCHAR(150),
    eligibility TEXT,
    income_limit DOUBLE,
    gender VARCHAR(50) DEFAULT 'ALL',
    minimum_age INT,
    maximum_age INT,
    education VARCHAR(255),
    benefits TEXT,
    required_documents TEXT,
    official_website VARCHAR(500),
    apply_link VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL
);

-- Seed Sample Government Schemes
INSERT INTO government_schemes (id, scheme_name, category, description, state, eligibility, income_limit, gender, minimum_age, maximum_age, education, benefits, required_documents, official_website, apply_link, is_active, created_at)
VALUES 
('d508ad00-a292-4f7f-8547-817a151b75a1', 
 'Pradhan Mantri Jan Dhan Yojana (PMJDY)', 
 'Financial Inclusion', 
 'National Mission for Financial Inclusion to ensure access to financial services, namely, basic savings & deposit accounts, remittance, credit, insurance, pension in an affordable manner.', 
 'ALL', 
 'Any Indian citizen above 10 years of age who does not have a basic savings account.', 
 9999999.0, 
 'ALL', 
 10, 
 100, 
 'ALL', 
 'Zero balance savings account, accidental insurance cover of ₹2 Lakh, overdraft facility of ₹10,000 for eligible account holders.', 
 'Aadhaar Card, PAN Card, or Voter ID Card.', 
 'https://pmjdy.gov.in', 
 'https://pmjdy.gov.in/account', 
 TRUE, 
 CURRENT_TIMESTAMP),

('d508ad00-a292-4f7f-8547-817a151b75a2', 
 'Ayushman Bharat PM-JAY', 
 'Healthcare', 
 'Provides health cover of up to ₹5 Lakh per family per year for secondary and tertiary care hospitalization across public and private empaneled hospitals in India.', 
 'ALL', 
 'Identified low-income families based on SECC 2011 database criteria.', 
 150000.0, 
 'ALL', 
 0, 
 120, 
 'ALL', 
 'Cashless treatment cover of up to ₹5 Lakh per family per year for hospitalization.', 
 'Aadhaar Card, Ration Card, PM-JAY Letter.', 
 'https://pmjay.gov.in', 
 'https://dashboard.pmjay.gov.in', 
 TRUE, 
 CURRENT_TIMESTAMP),

('d508ad00-a292-4f7f-8547-817a151b75a3', 
 'PM Kisan Samman Nidhi', 
 'Agriculture', 
 'An initiative by the government of India that provides up to ₹6,000 per year in three equal installments as direct income support to all landholding farmer families.', 
 'ALL', 
 'All small and marginal landholder farmer families who own cultivable land.', 
 300000.0, 
 'ALL', 
 18, 
 100, 
 'ALL', 
 'Direct income support of ₹6,000 per year directly transferred to bank accounts.', 
 'Landholding documents, Aadhaar Card, Bank Passbook.', 
 'https://pmkisan.gov.in', 
 'https://pmkisan.gov.in/RegistrationForm.aspx', 
 TRUE, 
 CURRENT_TIMESTAMP),

('d508ad00-a292-4f7f-8547-817a151b75a4', 
 'Lado Protsahan Yojana', 
 'Women Empowerment', 
 'A state-level initiative to encourage higher education among girls from weaker sections of society by providing financial incentives upon completing matriculation and intermediate studies.', 
 'Rajasthan', 
 'Girl students belonging to EWS/SC/ST families residing in Rajasthan.', 
 200000.0, 
 'FEMALE', 
 6, 
 25, 
 'Class 10 or Class 12', 
 'Financial assistance of ₹1,00,000 in savings bonds and cash incentives upon clearing exams.', 
 'Domicile certificate, Income certificate, School marksheet, Aadhaar Card.', 
 'https://sanjeevani.rajasthan.gov.in', 
 'https://sanjeevani.rajasthan.gov.in/lado', 
 TRUE, 
 CURRENT_TIMESTAMP);

-- 5. Scholarships Table
CREATE TABLE scholarships (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    education_level VARCHAR(100) NOT NULL,
    income_limit DOUBLE,
    state VARCHAR(150),
    eligibility TEXT,
    amount DOUBLE,
    deadline DATE,
    description TEXT,
    official_website VARCHAR(500),
    apply_link VARCHAR(500),
    created_at DATETIME NOT NULL
);

-- Seed Sample Scholarships
INSERT INTO scholarships (id, name, category, education_level, income_limit, state, eligibility, amount, deadline, description, official_website, apply_link, created_at)
VALUES
('f82b8a00-b531-4e8c-9642-127b151b75a1',
 'Post Matric Scholarship Scheme for SC Students',
 'Social Welfare',
 'Undergraduate',
 250000.0,
 'ALL',
 'Students belonging to Scheduled Castes whose family income does not exceed ₹2.5 LPA and pursuing post-matric courses.',
 15000.0,
 '2026-11-30',
 'Provides 100% tuition fee reimbursement and maintenance allowance for SC students pursuing professional courses.',
 'https://scholarships.gov.in',
 'https://scholarships.gov.in/fresh-application',
 CURRENT_TIMESTAMP),

('f82b8a00-b531-4e8c-9642-127b151b75a2',
 'National Means Cum Merit Scholarship',
 'Merit-based',
 'High School',
 350000.0,
 'ALL',
 'Class 9 students who secured at least 55% marks in Class 8, studying in government/aided schools, with family income under ₹3.5 LPA.',
 12000.0,
 '2026-10-15',
 'Assists talented secondary students of economically weaker sections to reduce drop-out rates after class VIII.',
 'https://scholarships.gov.in',
 'https://scholarships.gov.in/nmms',
 CURRENT_TIMESTAMP),

('f82b8a00-b531-4e8c-9642-127b151b75a3',
 'Central Sector Scheme of Scholarship for College and University Students',
 'Academic Excellence',
 'Undergraduate',
 450000.0,
 'ALL',
 'Students who are in the top 20th percentile in Class 12 board examinations, pursuing regular degree courses with family income under ₹4.5 LPA.',
 20000.0,
 '2026-12-15',
 'Provides financial assistance to meritorious college students to meet a part of their day-to-day expenses while pursuing higher studies.',
 'https://education.gov.in',
 'https://scholarships.gov.in/central-sector',
 CURRENT_TIMESTAMP);

-- 6. Chat Messages Table
-- 6. Chat Messages Table
CREATE TABLE chat_messages (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    user_message TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_chat_messages_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
);