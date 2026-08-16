#!/bin/bash

#############################################################################
# Create Kafka Topics for Malstrek
#############################################################################

create_topics() {
    log_info "Creating Kafka topics..."

    local topics=("start-line" "finish-line")
    local kafka_cmd="kafka-topics --bootstrap-server ${KAFKA_BROKER}"

    for topic in "${topics[@]}"; do
        # Check if topic already exists
        if eval "$kafka_cmd --list 2>/dev/null" | grep -q "^${topic}$"; then
            log_warn "Topic '$topic' already exists, skipping"
        else
            log_info "Creating topic: $topic"
            if eval "$kafka_cmd --create --topic \"$topic\" --partitions 1 --replication-factor 1 --if-not-exists"; then
                log_success "Topic '$topic' created"
            else
                log_error "Failed to create topic '$topic'"
                return 1
            fi
        fi
    done

    log_success "All topics are ready"
}
