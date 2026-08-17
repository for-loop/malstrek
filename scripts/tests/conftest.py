import json
import tempfile
from pathlib import Path
from typing import Dict

import pytest


@pytest.fixture
def temp_dir() -> Path:
    """Create a temporary directory for test files."""
    with tempfile.TemporaryDirectory() as tmp_dir:
        yield Path(tmp_dir)


@pytest.fixture
def test_config_file(temp_dir: Path) -> Path:
    """Create a test configuration file."""
    config = {
        "topics": [{"name": "test-topic", "partitions": 1, "replication_factor": 1}],
        "schemas": [{"subject": "test-schema", "file": "test-schema.avsc"}],
        "connectors": [{"name": "test-connector", "template": "test-connector.template.json"}],
    }
    config_path = temp_dir / "kafka-resources.json"
    with open(config_path, "w", encoding="utf-8") as file:
        json.dump(config, file)
    return config_path


@pytest.fixture
def test_schema_file(temp_dir: Path) -> Path:
    """Create a test Avro schema file."""
    schema = {
        "type": "record",
        "name": "TestRecord",
        "fields": [{"name": "id", "type": "int"}, {"name": "value", "type": "string"}],
    }
    schema_path = temp_dir / "test-schema.avsc"
    with open(schema_path, "w", encoding="utf-8") as file:
        json.dump(schema, file)
    return schema_path


@pytest.fixture
def test_connector_template(temp_dir: Path) -> Path:
    """Create a test connector template file."""
    template = {
        "name": "test-connector",
        "config": {
            "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
            "connection.url": "${CONNECTION_URL}",
            "connection.user": "${CONNECTION_USER}",
        },
    }
    template_path = temp_dir / "test-connector.template.json"
    with open(template_path, "w", encoding="utf-8") as file:
        json.dump(template, file)
    return template_path


@pytest.fixture
def mock_service_urls() -> Dict[str, str]:
    """Provide mock service URLs for testing."""
    return {
        "kafka_broker": "localhost:9092",
        "schema_registry_url": "http://localhost:8081",
        "kafka_connect_url": "http://localhost:8083",
    }
