create table if not exists sample
(
    id    serial PRIMARY KEY,
    name  VARCHAR(64) NOT NULL,
    data  text,
    value int default 0
);

create FUNCTION sample_trigger() RETURNS TRIGGER AS
'
    BEGIN
        IF (SELECT value
            FROM sample
            where id = NEW.id) > 1000
        THEN
            RAISE SQLSTATE ''23503'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER sample_value
    AFTER insert
    ON sample
    FOR EACH ROW
EXECUTE PROCEDURE sample_trigger();


CREATE TABLE IF NOT EXISTS PRODUCT
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(256) NOT NULL,
    description TEXT,
    brand_name  VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS COMPANY
(
    name         VARCHAR(256) PRIMARY KEY,
    phone_number VARCHAR(256) NOT NULL UNIQUE,
    country      VARCHAR(256),
    city         VARCHAR(256),
    zip_code     int CHECK ( zip_code > 0 ),
    street_info  VARCHAR(256)
);

CREATE TABLE IF NOT EXISTS COMPANY_EMAIL
(
    company_name VARCHAR(256),
    email        VARCHAR(256),
    PRIMARY KEY (company_name, email),
    FOREIGN KEY (company_name) REFERENCES COMPANY (name) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS PRODUCE
(
    id           SERIAL,
    company_name VARCHAR(256) NOT NULL,
    product_id   INT          NOT NULL,
    capacity     INT          NOT NULL CHECK ( capacity > 0 ),
    PRIMARY KEY (id),
    UNIQUE (company_name, product_id),
    FOREIGN KEY (company_name) REFERENCES Company (name) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS TRANSACTION
(
    id           SERIAL,
    company_name VARCHAR(256) NOT NULL,
    product_id   INT          NOT NULL CHECK ( product_id > 0 ),
    order_date   DATE         NOT NULL,
    amount       INT          NOT NULL CHECK ( amount > 0 ),
    PRIMARY KEY (id),
    FOREIGN KEY (company_name) REFERENCES Company (name) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES Product (id) ON DELETE CASCADE ON UPDATE CASCADE
);

create FUNCTION equal_zip_code_equal_city_trigger() RETURNS TRIGGER AS
'
    BEGIN
        IF (SELECT COUNT(*)
            FROM COMPANY c
            where c.zip_code = NEW.zip_code
              AND c.city NOT LIKE NEW.city) != 0
        THEN
            RAISE SQLSTATE ''23514'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER equal_zip_code_equal_city
    BEFORE INSERT OR UPDATE
    ON COMPANY
    FOR EACH ROW
EXECUTE PROCEDURE equal_zip_code_equal_city_trigger();

create FUNCTION summary_ordered_amount_less_then_capacity_trigger() RETURNS TRIGGER AS
'
    DECLARE
        capacity            int := (SELECT capacity
                                    FROM PRODUCE
                                    WHERE company_name = NEW.company_name
                                      AND product_id = NEW.product_id);
        DECLARE current_sum int := (SELECT SUM(amount)
                                    FROM TRANSACTION
                                    WHERE company_name = NEW.company_name
                                      AND product_id = NEW.product_id);
    BEGIN
        IF (capacity < current_sum + NEW.amount)
        THEN
            RAISE SQLSTATE ''23514'';
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER summary_ordered_amount_less_then_capacity
    BEFORE INSERT OR UPDATE
    ON TRANSACTION
    FOR EACH ROW
EXECUTE PROCEDURE summary_ordered_amount_less_then_capacity_trigger();

create FUNCTION delete_all_transactions_by_company_name_and_product_id(cur_company_name varchar, cur_product_id int)
    RETURNS VOID AS
'
    BEGIN
        DELETE
        FROM TRANSACTION
        WHERE company_name = cur_company_name
          AND product_id = cur_product_id;
    END
' LANGUAGE plpgsql;

create FUNCTION capacity_less_then_all_orders_amounts_trigger() RETURNS TRIGGER AS
'
    DECLARE
        current_sum int := (SELECT SUM(amount)
                            FROM TRANSACTION
                            WHERE company_name = NEW.company_name
                              AND product_id = NEW.product_id);
    BEGIN
        IF (NEW.capacity < OLD.capacity AND NEW.capacity < current_sum)
        THEN
            EXECUTE delete_all_transactions_by_company_name_and_product_id(NEW.company_name, NEW.product_id);
        END IF;
        RETURN NEW;
    END;
' LANGUAGE plpgsql;

create TRIGGER capacity_less_then_all_orders_amounts
    BEFORE UPDATE
    ON PRODUCE
    FOR EACH ROW
EXECUTE PROCEDURE capacity_less_then_all_orders_amounts_trigger();

create FUNCTION delete_produce_trigger() RETURNS TRIGGER AS
'
    BEGIN
        EXECUTE delete_all_transactions_by_company_name_and_product_id(OLD.company_name, OLD.product_id);
        RETURN OLD;
    END;
' LANGUAGE plpgsql;

CREATE TRIGGER delete_produce
    BEFORE DELETE
    ON PRODUCE
EXECUTE PROCEDURE delete_produce_trigger();


CREATE TABLE IF NOT EXISTS TRANSACTION_HISTORY
(
    id SERIAL,
    PRIMARY KEY (id)
);