#!/bin/bash

#############################################################################
# Create JDBC Sink Connectors in Kafka Connect
# 
# Uses template files with environment variable substitution.
# Safe to run multiple times - updates existing connectors.
#############################################################################

create_connectors() {
    log_info "Creating Kafka Connect JDBC Sink connectors..."

    local connectors=(
        "malstrek-starter-sink:/connector_configs/starter-sink.template.json"
        "malstrek-finisher-sink:/connector_configs/finisher-sink.template.json"
    )

    for connector_entry in "${connectors[@]}"; do
        IFS=':' read -r connector_name template_file <<< "$connector_entry"

        if [ ! -f "$template_file" ]; then
            log_error "Connector template not found: $template_file"
            return 1
        fi

        log_info "Processing connector: $connector_name"

        config=$(python3 - "$template_file" <<'PY'
import os, sys
from string import Template
path = sys.argv[1]
with open(path, 'r', encoding='utf-8') as f:
    raw = f.read()
print(Template(raw).safe_substitute(os.environ))
PY
)

        existing=$(curl -s "$KAFKA_CONNECT/connectors/$connector_name" 2>&1)

        if echo "$existing" | grep -q '"name"'; then
            log_warn "Connector '$connector_name' already exists, recreating..."

            delete_response=$(curl -s -w "\n%{http_code}" \
                -X DELETE \
                "$KAFKA_CONNECT/connectors/$connector_name" 2>&1)

            delete_http_code=$(echo "$delete_response" | tail -n1)
            delete_body=$(echo "$delete_response" | head -n-1)

            if [ "$delete_http_code" != "200" ] && [ "$delete_http_code" != "204" ]; then
                log_error "Failed to delete connector '$connector_name' (HTTP $delete_http_code): $delete_body"
                return 1
            fi

            log_info "Creating replacement connector: $connector_name"
        fi

        response=$(curl -s -w "\n%{http_code}" \
            -X POST \
            -H "Content-Type: application/json" \
            --data "$config" \
            "$KAFKA_CONNECT/connectors" 2>&1)

        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | head -n-1)

        if [ "$http_code" == "201" ] || [ "$http_code" == "200" ]; then
            log_success "Connector '$connector_name' is ready"
        else
            log_error "Failed to create connector '$connector_name' (HTTP $http_code): $body"
            return 1
        fi
    done

    log_success "All connectors are configured"
}
