-- ===============================================================================================
-- DOCUMENT SERVICE - DATA.SQL
-- Archivo de inicialización automática de Spring Boot
-- Se ejecuta automáticamente al iniciar la aplicación si las tablas están vacías
-- ===============================================================================================
-- UBICACIÓN: src/main/resources/data.sql
-- ===============================================================================================

-- ===============================================================================================
-- 1. POBLAR ESTADOS DE DOCUMENTOS
-- ===============================================================================================
-- IMPORTANTE: INSERT IGNORE evita errores si los datos ya existen
-- ===============================================================================================

INSERT IGNORE INTO estado (id, nombre) VALUES (1, 'PENDIENTE');
INSERT IGNORE INTO estado (id, nombre) VALUES (2, 'ACEPTADO');
INSERT IGNORE INTO estado (id, nombre) VALUES (3, 'RECHAZADO');
INSERT IGNORE INTO estado (id, nombre) VALUES (4, 'EN_REVISION');

-- ===============================================================================================
-- 2. POBLAR TIPOS DE DOCUMENTOS
-- ===============================================================================================

INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (1, 'DNI');
INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (2, 'PASAPORTE');
INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (3, 'LIQUIDACION_SUELDO');
INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (4, 'CERTIFICADO_ANTECEDENTES');
INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (5, 'CERTIFICADO_AFP');
INSERT IGNORE INTO tipo_doc (id, nombre) VALUES (6, 'CONTRATO_TRABAJO');

-- ===============================================================================================
-- NOTAS DE USO
-- ===============================================================================================
-- Este archivo se ejecuta automáticamente cuando:
-- 1. La aplicación Spring Boot inicia por primera vez
-- 2. spring.jpa.hibernate.ddl-auto=create o create-drop
-- 3. spring.sql.init.mode=always (en application.properties)
--
-- Para evitar errores:
-- - Usar INSERT IGNORE (MySQL) o INSERT ... ON CONFLICT DO NOTHING (PostgreSQL)
-- - No incluir datos de prueba aquí (solo datos maestros)
-- - Mantener IDs fijos para referencias desde otros servicios
-- ===============================================================================================
