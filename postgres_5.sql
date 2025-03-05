CREATE EXTENSION IF NOT EXISTS dblink;

-- Процедуры для создания и удаления базы данных
DROP PROCEDURE IF EXISTS sp_create_database(text, text);
CREATE OR REPLACE PROCEDURE sp_create_database(dbname TEXT, db_password TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM dblink_exec(
        'host=localhost port=5432 dbname=postgres user=' || quote_ident(current_user) ||
        ' password=' || quote_literal(db_password),
        'CREATE DATABASE ' || quote_ident(dbname)
    );
END;
$$;

DROP PROCEDURE IF EXISTS sp_drop_database(text, text);
CREATE OR REPLACE PROCEDURE sp_drop_database(dbname TEXT, db_password TEXT)
LANGUAGE plpgsql
AS $$
BEGIN
    PERFORM dblink_exec(
        'host=localhost port=5432 dbname=postgres user=' || quote_ident(current_user) ||
        ' password=' || quote_literal(db_password),
        'DROP DATABASE IF EXISTS ' || quote_ident(dbname)
    );
END;
$$;



-- CREATE DATABASE films_db;
