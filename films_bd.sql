-- 1. Создание таблицы 
DROP PROCEDURE IF EXISTS sp_create_table_gen(VARCHAR);
CREATE OR REPLACE PROCEDURE sp_create_table_gen(p_table VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'CREATE TABLE IF NOT EXISTS %I (
            id SERIAL PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            duration INT,
            genre VARCHAR(100),
            rating DOUBLE PRECISION
        )',
        p_table
    );
END;
$$;

-- 2. Очистка таблицы
DROP PROCEDURE IF EXISTS sp_clear_table_gen(VARCHAR);
CREATE OR REPLACE PROCEDURE sp_clear_table_gen(p_table VARCHAR)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format('TRUNCATE TABLE %I', p_table);
END;
$$;

-- 3. Добавление записи 
DROP PROCEDURE IF EXISTS sp_insert_film_gen(VARCHAR, VARCHAR, INT, VARCHAR, DOUBLE PRECISION);
CREATE OR REPLACE PROCEDURE sp_insert_film_gen(
    p_table VARCHAR,
    p_title VARCHAR,
    p_duration INT,
    p_genre VARCHAR,
    p_rating DOUBLE PRECISION
)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'INSERT INTO %I (title, duration, genre, rating) VALUES (%L, %s, %L, %s)',
        p_table,
        p_title,
        p_duration,
        p_genre,
        p_rating
    );
END;
$$;

-- 4. Получение всех записей
DROP PROCEDURE IF EXISTS sp_get_all_gen(VARCHAR, refcursor);
CREATE OR REPLACE PROCEDURE sp_get_all_gen(
    p_table VARCHAR,
    INOUT ref refcursor
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF ref IS NULL THEN
        ref := p_table || '_cursor';
    END IF;

    OPEN ref FOR EXECUTE format('SELECT * FROM %I', p_table);
END;
$$;

-- 5. Поиск записи по названию
DROP PROCEDURE IF EXISTS sp_search_film_gen(VARCHAR, VARCHAR, refcursor);
CREATE OR REPLACE PROCEDURE sp_search_film_gen(
    p_table VARCHAR,
    p_title VARCHAR,
    INOUT ref refcursor
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF ref IS NULL THEN
        ref := p_table || '_search_cursor';
    END IF;

    OPEN ref FOR EXECUTE format('SELECT * FROM %I WHERE title = %L', p_table, p_title);
END;
$$;

-- 6. Обновление записи 
DROP PROCEDURE IF EXISTS sp_update_film_gen(VARCHAR, INT, VARCHAR, INT, VARCHAR, DOUBLE PRECISION);
CREATE OR REPLACE PROCEDURE sp_update_film_gen(
    p_table VARCHAR,
    p_id INT,
    p_title VARCHAR,
    p_duration INT,
    p_genre VARCHAR,
    p_rating DOUBLE PRECISION
)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'UPDATE %I SET title = %L, duration = %s, genre = %L, rating = %s WHERE id = %s',
        p_table,
        p_title,
        p_duration,
        p_genre,
        p_rating,
        p_id
    );
END;
$$;

-- 7. Удаление записи по названию
DROP PROCEDURE IF EXISTS sp_delete_film_gen(VARCHAR, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_delete_film_gen(
    p_table VARCHAR,
    p_title VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE format(
        'DELETE FROM %I WHERE title = %L',
        p_table,
        p_title
    );
END;
$$;

-- 8. Создание нового пользователя БД 
DROP PROCEDURE IF EXISTS sp_create_db_user(VARCHAR, VARCHAR, VARCHAR);
CREATE OR REPLACE PROCEDURE sp_create_db_user(
    p_username VARCHAR,
    p_password VARCHAR,
    p_role VARCHAR
)
LANGUAGE plpgsql
AS $$
BEGIN
    EXECUTE 'CREATE USER ' || quote_ident(p_username) || ' WITH PASSWORD ' || quote_literal(p_password);
    IF p_role = 'Admin' THEN
        EXECUTE 'GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO ' || quote_ident(p_username);
        EXECUTE 'GRANT ALL PRIVILEGES ON DATABASE ' || current_database() || ' TO ' || quote_ident(p_username);
    ELSIF p_role = 'Guest' THEN
        EXECUTE 'GRANT SELECT ON ALL TABLES IN SCHEMA public TO ' || quote_ident(p_username);
    END IF;
END;
$$;
