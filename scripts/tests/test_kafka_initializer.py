from unittest.mock import patch, MagicMock
from pathlib import Path
import pytest

from scripts.kafka_initializer import KafkaInitializer
from scripts.resources.kafka_topic import KafkaTopic
from scripts.resources.kafka_schema import KafkaSchema
from scripts.resources.kafka_connector import KafkaConnector


class TestKafkaInitializer:
    """Test suite for KafkaInitializer."""

    @pytest.fixture
    def initializer(self, temp_dir: Path, test_config_file: Path) -> KafkaInitializer:
        """Create a test KafkaInitializer instance."""
        return KafkaInitializer(
            kafka_broker="localhost:9092",
            schema_registry_url="http://localhost:8081",
            kafka_connect_url="http://localhost:8083",
            config_file_path=test_config_file,
            base_dir=temp_dir,
        )

    @patch("scripts.kafka_initializer.ServiceMonitor")
    @patch.object(KafkaInitializer, "create_resources")
    def test_run_completes_all_steps(
        self,
        mock_create_resources: MagicMock,
        mock_service_monitor_class: MagicMock,
        initializer: KafkaInitializer,
    ) -> None:
        """Test that run() executes all initialization steps."""
        mock_monitor_instance = MagicMock()
        mock_monitor_instance.wait_for_broker.return_value = True
        mock_monitor_instance.wait_for_schema_registry.return_value = True
        mock_monitor_instance.wait_for_kafka_connect.return_value = True
        mock_service_monitor_class.return_value = mock_monitor_instance

        mock_create_resources.return_value = True

        reinitialize_initializer = KafkaInitializer(
            kafka_broker="localhost:9092",
            schema_registry_url="http://localhost:8081",
            kafka_connect_url="http://localhost:8083",
            config_file_path=initializer.config_file_path,
            base_dir=initializer.base_dir,
        )

        assert reinitialize_initializer.run() is True
        assert mock_create_resources.call_count == 3

    @patch("scripts.kafka_initializer.ServiceMonitor")
    def test_run_returns_false_if_broker_unavailable(
        self, mock_service_monitor_class: MagicMock, initializer: KafkaInitializer
    ) -> None:
        """Test that run() returns False if broker is unavailable."""
        mock_monitor_instance = MagicMock()
        mock_monitor_instance.wait_for_broker.return_value = False
        mock_service_monitor_class.return_value = mock_monitor_instance

        reinitialize_initializer = KafkaInitializer(
            kafka_broker="localhost:9092",
            schema_registry_url="http://localhost:8081",
            kafka_connect_url="http://localhost:8083",
            config_file_path=initializer.config_file_path,
            base_dir=initializer.base_dir,
        )

        assert reinitialize_initializer.run() is False

    def test_create_resources_calls_create_on_each_resource(
        self, initializer: KafkaInitializer
    ) -> None:
        """Test that create_resources calls create on each resource."""
        mock_resource_one = MagicMock()
        mock_resource_one.create.return_value = True
        mock_resource_two = MagicMock()
        mock_resource_two.create.return_value = True

        resources = [mock_resource_one, mock_resource_two]
        assert initializer.create_resources(resources, "test-resources")

        mock_resource_one.create.assert_called_once()
        mock_resource_two.create.assert_called_once()

    def test_create_resources_returns_false_on_resource_failure(
        self, initializer: KafkaInitializer
    ) -> None:
        """Test that create_resources stops on first failure."""
        mock_resource_one = MagicMock()
        mock_resource_one.create.return_value = False
        mock_resource_two = MagicMock()
        mock_resource_two.create.return_value = True

        resources = [mock_resource_one, mock_resource_two]
        assert not initializer.create_resources(resources, "test-resources")

        mock_resource_two.create.assert_not_called()
