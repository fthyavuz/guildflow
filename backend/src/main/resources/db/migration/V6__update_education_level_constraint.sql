-- =============================================
-- V6: Update education_level constraint to include UNIVERSITY
-- =============================================

ALTER TABLE mentor_classes 
DROP CONSTRAINT mentor_classes_education_level_check;

ALTER TABLE mentor_classes 
ADD CONSTRAINT mentor_classes_education_level_check 
CHECK (education_level IN ('PRIMARY', 'SECONDARY', 'HIGH_SCHOOL', 'UNIVERSITY'));
