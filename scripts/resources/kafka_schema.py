import logging
from pathlib import Path

import requests

from scripts.resources.kafka_resource import KafkaResource
from scripts.config.kafka_schema_config import KafkaSchemaConfig

logger = logging.getLogger(__name__)


class KafkaSchema(KafkaResource):
    """Represents an Avro schema resource."""

    def __init__(self, schema_registry_url: str, config: KafkaSchemaConfig) -> None:
        """
        Initialize Kafka schema.

        Args:
            schema_registry_url: Schema Registry URL
            config: Schema configuration
        """
        self.schema_registry_url = schema_registry_url
        self.config = config

    def exists(self) -> bool:
        """
        Check if schema is registered.

        Returns:
            True if schema exists, False otherwise
        """
        try:
            response = requests.get(
                f"{self.schema_registry_url}/subjects/" f"{self.config.subject}-value/versions",
                timeout=5,
            )
            return response.status_code == 200
        except requests.RequestException:
            return False

    def create(self) -> bool:
        """
        Register schema if not already registered.

        Returns:
            True on success, False on failure
        """
        logger.info(f"Schema config file: {self.config.file}")
        if not self.config.file:
            logger.error(f"Schema file not found: {self.config.file}")
            return False

        logger.info(f"Registering schema: {self.config.subject}")

        try:
            with open(self.config.file, "r", encoding="utf-8") as file:
                schema_text = file.read()

            payload = {"schema": schema_text}

            response = requests.post(
                f"{self.schema_registry_url}/subjects/" f"{self.config.subject}-value/versions",
                json=payload,
                headers={"Content-Type": "application/vnd.schemaregistry.v1+json"},
                timeout=10,
            )

            if response.status_code in [200, 409]:
                logger.info(
                    f'✅ Schema "{self.config.subject}" registered '
                    f"(HTTP {response.status_code})"
                )
                return True
            else:
                logger.error(
                    f'Failed to register schema "{self.config.subject}" '
                    f"(HTTP {response.status_code}): {response.text}"
                )
                return False

        except Exception as exception:
            logger.error(f'Error registering schema "{self.config.subject}": ' f"{exception}")
            return False
