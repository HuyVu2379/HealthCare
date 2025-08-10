#!/bin/bash

set -e
set -u

# Function to create a database if it doesn't exist
function create_database() {
    local database=$1
    echo "Creating database '$database'"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        CREATE DATABASE $database;
        GRANT ALL PRIVILEGES ON DATABASE $database TO $POSTGRES_USER;
EOSQL
}

# Check if POSTGRES_MULTIPLE_DATABASES environment variable exists
if [ -n "$POSTGRES_MULTIPLE_DATABASES" ]; then
    echo "Multiple database creation requested: $POSTGRES_MULTIPLE_DATABASES"
    
    # Convert comma-separated string to array
    for db in $(echo $POSTGRES_MULTIPLE_DATABASES | tr ',' ' '); do
        # Check if the database already exists
        if [ -z "$(psql -U "$POSTGRES_USER" -lqt | cut -d \| -f 1 | grep -w $db)" ]; then
            # Database doesn't exist, create it
            create_database $db
        else
            echo "Database '$db' already exists. Skipping creation."
        fi
    done

    echo "Multiple databases created"
fi