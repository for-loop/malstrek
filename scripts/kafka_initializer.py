import logging
from pathlib import Path
from typing import List

from scripts.config.kafka_initialization_config import KafkaInitializationConfig
from scripts.resources.kafka_resource import KafkaResource
from scripts.resources.kafka_topic import KafkaTopic
from scripts.resources.kafka_schema import KafkaSchema
from scripts.resources.kafka_connector import KafkaConnector
from scripts.services.service_monitor import ServiceMonitor

logger = logging.getLogger(__name__)


class KafkaInitializer:
    """Orchestrates Kafka infrastructure initialization."""

    def __init__(
        self,
        kafka_broker: str = "broker:29092",
        schema_registry_url: str = "http://schema-registry:8081",
        kafka_connect_url: str = "http://connect:8083",
        config_file_path: Path = None,
        base_dir: Path = None,
    ) -> None:
        """
        Initialize Kafka initializer.

        Args:
            kafka_broker: Kafka broker address
            schema_registry_url: Schema Registry URL
            kafka_connect_url: Kafka Connect URL
            config_file_path: Path to configuration JSON file
            base_dir: Base directory for resolving relative paths
        """
        self.kafka_broker = kafka_broker
        self.schema_registry_url = schema_registry_url
        self.kafka_connect_url = kafka_connect_url

        if config_file_path is None:
            config_file_path = Path(__file__).parent / "kafka-resources.json"
        if base_dir is None:
            base_dir = Path("/app")

        self.config_file_path = config_file_path
        self.config = KafkaInitializationConfig(config_file_path)
        self.base_dir = base_dir
        self.monitor = ServiceMonitor(
            self.kafka_broker, self.schema_registry_url, self.kafka_connect_url
        )

    def create_resources(self, resources: List[KafkaResource], resource_type: str) -> bool:
        """
        Generic method to create any Kafka resource type.

        Args:
            resources: List of resources to create
            resource_type: Type name for logging

        Returns:
            True if all resources created successfully, False otherwise
        """
        logger.info(f"Creating {resource_type}...")

        for resource in resources:
            if not resource.create():
                return False

        logger.info(f"✅ All {resource_type} are ready")
        return True

    def run(self) -> bool:
        """
        Execute the full Kafka initialization sequence.

        Returns:
            True if all steps succeeded, False otherwise
        """
        logger.info("Starting Kafka initialization for Malstrek...")

        if not self.monitor.wait_for_broker():
            return False

        # Create topics
        topic_configs = self.config.get_topic_configs()
        topics = [KafkaTopic(self.kafka_broker, config) for config in topic_configs]
        if not self.create_resources(topics, "topics"):
            return False

        # Register schemas
        if not self.monitor.wait_for_schema_registry():
            return False

        schema_configs = self.config.get_schema_configs(self.base_dir)
        schemas = [KafkaSchema(self.schema_registry_url, config) for config in schema_configs]
        if not self.create_resources(schemas, "schemas"):
            return False

        # Create connectors
        if not self.monitor.wait_for_kafka_connect():
            return False

        connector_configs = self.config.get_connector_configs(self.base_dir)
        connectors = [
            KafkaConnector(self.kafka_connect_url, config) for config in connector_configs
        ]
        if not self.create_resources(connectors, "connectors"):
            return False

        logger.info("✅ Kafka initialization complete!")
        logger.info("Malstrek is ready to use")
        return True
