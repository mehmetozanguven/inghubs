--- for spring modulith
CREATE TABLE IF NOT EXISTS event_publication
(
  id               UUID NOT NULL,
  listener_id      TEXT NOT NULL,
  event_type       TEXT NOT NULL,
  serialized_event TEXT NOT NULL,
  publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
  completion_date  TIMESTAMP WITH TIME ZONE,
  PRIMARY KEY (id)
);
CREATE INDEX IF NOT EXISTS event_publication_serialized_event_hash_idx ON event_publication USING hash(serialized_event);
CREATE INDEX IF NOT EXISTS event_publication_by_completion_date_idx ON event_publication (completion_date);

--- for shedlock
CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_at TIMESTAMP WITH TIME ZONE NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);


CREATE TABLE customers (
    email character varying(100) UNIQUE,
    password character varying(100),
    name character varying(30) NOT NULL,
    surname character varying(30) NOT NULL,
    tckn character varying(30) NOT NULL,
    id character varying(60) PRIMARY KEY NOT NULL,
    created_date timestamp(6) with time zone,
    created_date_in_ms BIGINT NOT NULL,
    created_date_offset smallint NOT NULL,
    last_update_date timestamp(6) with time zone NOT NULL,
    last_update_date_in_ms BIGINT NOT NULL,
    last_update_offset smallint NOT NULL,
    entity_version_id bigint,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE customer_roles (
    role character varying(60) check (role in ('CUSTOMER','EMPLOYEE')),
    id character varying(60) PRIMARY KEY NOT NULL,
    created_date timestamp(6) with time zone,
    created_date_in_ms BIGINT NOT NULL,
    created_date_offset smallint NOT NULL,
    last_update_date timestamp(6) with time zone NOT NULL,
    last_update_date_in_ms BIGINT NOT NULL,
    last_update_offset smallint NOT NULL,
    entity_version_id bigint,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE customer_roles_join (
    customer_id character varying(60) NOT NULL,
    customer_role_id character varying(60) NOT NULL,
    PRIMARY KEY(customer_id, customer_role_id),
    CONSTRAINT fkawad3tmplm8jkp5ykab4cyhg3
    FOREIGN KEY(customer_id) REFERENCES customers(id),
    CONSTRAINT fkawad3tmplm8jkp5ykab4cyhg2
    FOREIGN KEY(customer_role_id) REFERENCES customer_roles(id)
);


CREATE TABLE wallets (
    customer_id character varying(60) NOT NULL,
    wallet_name character varying(100) NOT NULL,
    currency_type character varying(30) check (currency_type in ('TRY','USD', 'EURO')),
    active_for_shopping BOOLEAN NOT NULL,
    active_for_withdraw BOOLEAN NOT NULL,
    balance NUMERIC(19, 2) NOT NULL,
    usable_balance NUMERIC(19, 2) NOT NULL,
    id character varying(60) PRIMARY KEY NOT NULL,
    created_date timestamp(6) with time zone,
    created_date_in_ms BIGINT NOT NULL,
    created_date_offset smallint NOT NULL,
    last_update_date timestamp(6) with time zone NOT NULL,
    last_update_date_in_ms BIGINT NOT NULL,
    last_update_offset smallint NOT NULL,
    entity_version_id bigint,
    active BOOLEAN DEFAULT TRUE
);

CREATE TABLE wallet_transactions (
    wallet_id character varying(60),
    transaction_type character varying(60),
    transaction_status character varying(60),
    amount NUMERIC(19, 2),
    currency_type character varying(30) check (currency_type in ('TRY','USD', 'EURO')),
    opposite_party character varying(30),
    opposite_party_type character varying(30),
    processed_date timestamp(6) with time zone,
    processed_date_offset smallint,
    expiration_time timestamp(6) with time zone NOT NULL,
    expiration_time_offset smallint NOT NULL,
    info_field jsonb,
    id character varying(60) PRIMARY KEY NOT NULL,
    created_date timestamp(6) with time zone,
    created_date_in_ms BIGINT NOT NULL,
    created_date_offset smallint NOT NULL,
    last_update_date timestamp(6) with time zone NOT NULL,
    last_update_date_in_ms BIGINT NOT NULL,
    last_update_offset smallint NOT NULL,
    entity_version_id bigint,
    active BOOLEAN DEFAULT TRUE
);
