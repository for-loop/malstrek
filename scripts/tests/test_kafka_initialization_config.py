from pathlib import Path
import json
import pytest

from scripts.config.kafka_initialization_config import KafkaInitializationConfig


class TestKafkaInitializationConfig:
    """Test suite for KafkaInitializationConfig."""

    @pytest.fixture
    def config(self, temp_dir: Path, test_config_file: Path) -> KafkaInitializationConfig:
        """Create a test configuration."""
        return KafkaInitializationConfig(test_config_file)

    def test_get_topic_configs_returns_all_topics(self, config: KafkaInitializationConfig) -> None:
        """Test that get_topic_configs returns all configured topics."""
        topics = config.get_topic_configs()
        assert len(topics) == 1
        assert topics[0].name == "test-topic"

    def test_get_schema_configs_returns_all_schemas(
        self, config: KafkaInitializationConfig, temp_dir: Path
    ) -> None:
        """Test that get_schema_configs returns all configured schemas."""
        schemas = config.get_schema_configs(temp_dir)
        assert len(schemas) == 1
        assert schemas[0].subject == "test-schema"

    def test_get_connector_configs_returns_all_connectors(
        self, config: KafkaInitializationConfig, temp_dir: Path
    ) -> None:
        """Test that get_connector_configs returns all connectors."""
        connectors = config.get_connector_configs(temp_dir)
        assert len(connectors) == 1
        assert connectors[0].name == "test-connector"
