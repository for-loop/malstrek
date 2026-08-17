from unittest.mock import patch, MagicMock, mock_open
from pathlib import Path
import pytest
import requests

from scripts.resources.kafka_schema import KafkaSchema
from scripts.config.kafka_schema_config import KafkaSchemaConfig


class TestKafkaSchema:
    """Test suite for KafkaSchema."""

    @pytest.fixture
    def schema_config(self, temp_dir: Path) -> KafkaSchemaConfig:
        """Create a test schema configuration."""
        schema_file = temp_dir / "test.avsc"
        schema_file.write_text('{"type":"string"}', encoding="utf-8")
        return KafkaSchemaConfig({"subject": "test-schema", "file": schema_file})

    @pytest.fixture
    def kafka_schema(self, schema_config: KafkaSchemaConfig) -> KafkaSchema:
        """Create a test KafkaSchema instance."""
        return KafkaSchema(schema_registry_url="http://localhost:8081", config=schema_config)

    @patch("requests.get")
    def test_exists_returns_true_when_schema_found(
        self, mock_get: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that exists() returns True when schema is found."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_get.return_value = mock_response

        assert kafka_schema.exists() is True

    @patch("requests.get")
    def test_exists_returns_false_when_schema_not_found(
        self, mock_get: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that exists() returns False when schema not found."""
        mock_response = MagicMock()
        mock_response.status_code = 404
        mock_get.return_value = mock_response

        assert kafka_schema.exists() is False

    @patch("requests.get")
    def test_exists_returns_false_on_exception(
        self, mock_get: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that exists() returns False on request exception."""
        mock_get.side_effect = requests.RequestException("Connection failed")

        assert kafka_schema.exists() is False

    @patch("requests.post")
    def test_create_registers_schema_successfully(
        self, mock_post: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that create() registers schema successfully."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_post.return_value = mock_response

        assert kafka_schema.create() is True
        mock_post.assert_called_once()

    @patch("requests.post")
    def test_create_handles_conflict_status(
        self, mock_post: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that create() treats 409 as success."""
        mock_response = MagicMock()
        mock_response.status_code = 409
        mock_post.return_value = mock_response

        assert kafka_schema.create() is True

    @patch("requests.post")
    def test_create_returns_false_on_error_status(
        self, mock_post: MagicMock, kafka_schema: KafkaSchema
    ) -> None:
        """Test that create() returns False on error status."""
        mock_response = MagicMock()
        mock_response.status_code = 500
        mock_response.text = "Internal server error"
        mock_post.return_value = mock_response

        assert kafka_schema.create() is False
