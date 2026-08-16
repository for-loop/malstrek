#!/bin/bash

#############################################################################
# Malstrek Kafka Initialization Script
# 
# This script orchestrates the setup of Kafka infrastructure for malstrek:
# 1. Waits for dependent services to be healthy
# 2. Creates Kafka topics
# 3. Registers Avro schemas with Schema Registry
# 4. Creates JDBC Sink connectors
#
# This runs automatically as part of `docker compose up` via the init-kafka
# service. All configuration is sourced from .env file.
#############################################################################

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration - Use container environment variables
KAFKA_BROKER="${KAFKA_BOOTSTRAP_SERVERS:-broker:29092}"
SCHEMA_REGISTRY="${KAFKA_SCHEMA_REGISTRY_URL:-http://schema-registry:8081}"
KAFKA_CONNECT="${KAFKA_CONNECT_URL:-http://connect:8083}"
MAX_RETRIES=30
RETRY_DELAY=2

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅${NC} $1"
}

# Wait for a service to be healthy
wait_for_service() {
    local service_name=$1
    local check_command=$2
    local attempt=1

    log_info "Waiting for $service_name..."

    while [ $attempt -le $MAX_RETRIES ]; do
        if eval "$check_command" > /dev/null 2>&1; then
            log_success "$service_name is ready"
            return 0
        fi

        echo -ne "\r  Attempt $attempt/$MAX_RETRIES..."
        attempt=$((attempt + 1))
        sleep $RETRY_DELAY
    done

    log_error "$service_name did not become ready after $((MAX_RETRIES * RETRY_DELAY)) seconds"
    return 1
}

wait_for_kafka_broker() {
    local host=${KAFKA_BROKER%:*}
    local port=${KAFKA_BROKER##*:}
    wait_for_service "Kafka Broker" "python3 -c 'import socket,sys; s=socket.socket(); s.settimeout(2); s.connect((\"$host\", int($port))); s.close()'"
}

# Source the helper scripts
source "$SCRIPT_DIR/create-topics.sh"
source "$SCRIPT_DIR/register-schemas.sh"
source "$SCRIPT_DIR/create-connectors.sh"

main() {
    log_info "Starting Kafka initialization for Malstrek..."

    wait_for_kafka_broker

    # Create topics
    create_topics

    # Only wait for the next dependency once the previous stage succeeded.
    wait_for_service "Schema Registry" "curl -fsS $SCHEMA_REGISTRY/subjects > /dev/null"
    register_schemas

    wait_for_service "Kafka Connect" "curl -fsS $KAFKA_CONNECT/health > /dev/null"
    create_connectors

    log_success "Kafka initialization complete!"
    log_info "Malstrek is ready to use"
}

main "$@"
