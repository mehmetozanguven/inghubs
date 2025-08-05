#!/bin/bash
set -e

# Connect to PostgreSQL and create the database if it doesn't exist
# We use the default 'postgres' database to connect initially
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "postgres" <<-EOSQL
    CREATE DATABASE example_ing_test_db;
EOSQL