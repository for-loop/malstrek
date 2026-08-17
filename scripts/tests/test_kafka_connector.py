from unittest.mock import patch, MagicMock
from pathlib import Path
import json
import pytest
import requests

from scripts.resources.kafka_connector import KafkaConnector
from scripts.config.kafka_connector_config import KafkaConnectorConfig


class TestKafkaConnector:
    """Test suite for KafkaConnector."""

    @pytest.fixture
    def connector_config(self, temp_dir: Path) -> KafkaConnectorConfig:
        """Create a test connector configuration."""
        template_file = temp_dir / "test-connector.template.json"
        template = {
            "name": "test-connector",
            "config": {
                "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
                "connection.url": "${CONNECTION_URL}",
            },
        }
        template_file.write_text(json.dumps(template), encoding="utf-8")
        return KafkaConnectorConfig({"name": "test-connector", "template": template_file})

    @pytest.fixture
    def kafka_connector(self, connector_config: KafkaConnectorConfig) -> KafkaConnector:
        """Create a test KafkaConnector instance."""
        return KafkaConnector(kafka_connect_url="http://localhost:8083", config=connector_config)

    @patch("requests.get")
    def test_exists_returns_true_when_connector_found(
        self, mock_get: MagicMock, kafka_connector: KafkaConnector
    ) -> None:
        """Test that exists() returns True when connector found."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_get.return_value = mock_response

        assert kafka_connector.exists() is True

    @patch("requests.get")
    def test_exists_returns_false_when_connector_not_found(
        self, mock_get: MagicMock, kafka_connector: KafkaConnector
    ) -> None:
        """Test that exists() returns False when connector not found."""
        mock_response = MagicMock()
        mock_response.status_code = 404
        mock_get.return_value = mock_response

        assert kafka_connector.exists() is False

    @patch("requests.post")
    @patch("scripts.resources.kafka_connector.KafkaConnector.exists")
    def test_create_creates_new_connector(
        self, mock_exists: MagicMock, mock_post: MagicMock, kafka_connector: KafkaConnector
    ) -> None:
        """Test that create() creates connector if not exists."""
        mock_exists.return_value = False
        mock_response = MagicMock()
        mock_response.status_code = 201
        mock_post.return_value = mock_response

        assert kafka_connector.create() is True

    @patch("requests.delete")
    @patch("requests.post")
    @patch("scripts.resources.kafka_connector.KafkaConnector.exists")
    def test_create_recreates_existing_connector(
        self,
        mock_exists: MagicMock,
        mock_post: MagicMock,
        mock_delete: MagicMock,
        kafka_connector: KafkaConnector,
    ) -> None:
        """Test that create() deletes and recreates existing connector."""
        mock_exists.return_value = True
        mock_delete_response = MagicMock()
        mock_delete_response.status_code = 204
        mock_delete.return_value = mock_delete_response
        mock_post_response = MagicMock()
        mock_post_response.status_code = 201
        mock_post.return_value = mock_post_response

        assert kafka_connector.create() is True
        mock_delete.assert_called_once()
        mock_post.assert_called_once()
