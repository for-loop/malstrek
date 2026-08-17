import json
import logging
from pathlib import Path
from typing import List

from scripts.config.kafka_topic_config import KafkaTopicConfig
from scripts.config.kafka_schema_config import KafkaSchemaConfig
from scripts.config.kafka_connector_config import KafkaConnectorConfig

logger = logging.getLogger(__name__)


class KafkaInitializationConfig:
    """Loads and manages Kafka initialization configuration."""

    def __init__(self, config_file_path: Path) -> None:
        """
        Load configuration from JSON file.

        Args:
            config_file_path: Path to configuration JSON file
        """
        with open(config_file_path, "r", encoding="utf-8") as file:
            self.config = json.load(file)

    def get_topic_configs(self) -> List[KafkaTopicConfig]:
        """
        Get topic configurations.

        Returns:
            List of topic configurations
        """
        topics = self.config.get("topics", [])
        return [KafkaTopicConfig(topic) for topic in topics]

    def get_schema_configs(self, base_dir: Path) -> List[KafkaSchemaConfig]:
        """
        Get schema configurations with resolved file paths.

        Args:
            base_dir: Base directory for resolving relative paths

        Returns:
            List of schema configurations with absolute paths
        """
        schemas = self.config.get("schemas", [])
        schema_configs = []

        for schema_dict in schemas:
            schema_config = KafkaSchemaConfig(schema_dict)
            schema_file_path = schema_dict.get("file", "")

            # Only prepend base_dir if path is relative
            if schema_file_path and not Path(schema_file_path).is_absolute():
                schema_file_path = str(base_dir / schema_file_path)

            schema_config.file = schema_file_path
            schema_configs.append(schema_config)

        return schema_configs

    def get_connector_configs(self, base_dir: Path) -> List[KafkaConnectorConfig]:
        """
        Get connector configurations with resolved file paths.

        Args:
            base_dir: Base directory for resolving relative paths

        Returns:
            List of connector configurations with absolute paths
        """
        connectors = self.config.get("connectors", [])
        connector_configs = []

        for connector_dict in connectors:
            connector_config = KafkaConnectorConfig(connector_dict)
            template_file_path = connector_dict.get("template", "")

            # Only prepend base_dir if path is relative
            if template_file_path and not Path(template_file_path).is_absolute():
                template_file_path = str(base_dir / template_file_path)

            connector_config.template = template_file_path
            connector_configs.append(connector_config)

        return connector_configs
