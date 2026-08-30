"""Database Schema Initialization Script for Neon PostgreSQL

Creates the relational tables for Cogniva:
- patients
- patient_conditions
- emergency_contacts
- reminders
- activities
- assessments
"""

import sys
import os
import psycopg
from dotenv import load_dotenv

# Ensure project root is in sys.path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from backend.database import get_database_url

SCHEMA_SQL = """
-- 1. Patients Table
CREATE TABLE IF NOT EXISTS patients (
    id VARCHAR(128) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL DEFAULT 'Patient',
    date_of_birth VARCHAR(64),
    age INTEGER,
    gender VARCHAR(32),
    email VARCHAR(255),
    phone VARCHAR(64),
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Patient Conditions Table
CREATE TABLE IF NOT EXISTS patient_conditions (
    id VARCHAR(128) PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    condition VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Emergency Contacts Table
CREATE TABLE IF NOT EXISTS emergency_contacts (
    id VARCHAR(128) PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    relationship VARCHAR(128),
    phone VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. Reminders Table
CREATE TABLE IF NOT EXISTS reminders (
    id VARCHAR(128) PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    reminder_date VARCHAR(64),
    reminder_time VARCHAR(64),
    repeat_type VARCHAR(64) DEFAULT 'Daily',
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP WITH TIME ZONE
);

-- 5. Activities (Game Sessions) Table
CREATE TABLE IF NOT EXISTS activities (
    id VARCHAR(128) PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    activity_name VARCHAR(255) NOT NULL,
    score DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    category VARCHAR(64) DEFAULT 'Memory',
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Assessments Table
CREATE TABLE IF NOT EXISTS assessments (
    id VARCHAR(128) PRIMARY KEY,
    patient_id VARCHAR(128) NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    assessment_name VARCHAR(255) NOT NULL,
    memory_score DOUBLE PRECISION DEFAULT 0.5,
    attention_score DOUBLE PRECISION DEFAULT 0.5,
    overall_score DOUBLE PRECISION DEFAULT 0.5,
    completed_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Caregivers Table
CREATE TABLE IF NOT EXISTS caregivers (
    id VARCHAR(128) PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL DEFAULT 'Caregiver',
    email VARCHAR(255),
    phone VARCHAR(64),
    patient_relationship VARCHAR(128),
    linked_patient_id VARCHAR(128),
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indices for performance
CREATE INDEX IF NOT EXISTS idx_activities_patient ON activities(patient_id, completed_at DESC);
CREATE INDEX IF NOT EXISTS idx_assessments_patient ON assessments(patient_id, completed_at DESC);
CREATE INDEX IF NOT EXISTS idx_reminders_patient ON reminders(patient_id);
CREATE INDEX IF NOT EXISTS idx_conditions_patient ON patient_conditions(patient_id);
CREATE INDEX IF NOT EXISTS idx_contacts_patient ON emergency_contacts(patient_id);
CREATE INDEX IF NOT EXISTS idx_caregivers_linked_patient ON caregivers(linked_patient_id);
"""

def init_neon_database():
    url = get_database_url()
    if not url:
        print("ERROR: DATABASE_URL not set in .env")
        sys.exit(1)
        
    print(f"Connecting to Neon PostgreSQL...")
    with psycopg.connect(url) as conn:
        with conn.cursor() as cur:
            cur.execute(SCHEMA_SQL)
            conn.commit()
            print("Successfully initialized all 6 tables and indices in Neon PostgreSQL!")

if __name__ == "__main__":
    init_neon_database()
