#!/bin/bash

#############################################################################
# Register Avro Schemas with Confluent Schema Registry
#############################################################################

register_schemas() {
    log_info "Registering Avro schemas..."

    local schemas=(
        "start-line:/avro/start-line.avsc"
        "finish-line:/avro/finish-line.avsc"
    )

    for schema_entry in "${schemas[@]}"; do
        IFS=':' read -r subject schema_file <<< "$schema_entry"

        if [ ! -f "$schema_file" ]; then
            log_error "Schema file not found: $schema_file"
            return 1
        fi

        log_info "Registering schema: $subject from $schema_file"

        payload=$(python3 - "$schema_file" <<'PY'
import json, sys
with open(sys.argv[1], 'r', encoding='utf-8') as f:
    schema_text = f.read()
print(json.dumps({"schema": schema_text}))
PY
)

        response=$(curl -s -w "\n%{http_code}" \
            -X POST \
            -H "Content-Type: application/vnd.schemaregistry.v1+json" \
            --data "$payload" \
            "$SCHEMA_REGISTRY/subjects/${subject}-value/versions" 2>&1)

        http_code=$(echo "$response" | tail -n1)
        body=$(echo "$response" | head -n-1)

        if [ "$http_code" == "200" ] || [ "$http_code" == "409" ]; then
            log_success "Schema '$subject' registered (HTTP $http_code)"
        else
            log_error "Failed to register schema '$subject' (HTTP $http_code): $body"
            return 1
        fi
    done

    log_success "All schemas are registered"
}
