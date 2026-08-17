import json
import logging
from pathlib import Path
from string import Template

import requests

from scripts.resources.kafka_resource import KafkaResource
from scripts.config.kafka_connector_config import KafkaConnectorConfig

logger = logging.getLogger(__name__)


class KafkaConnector(KafkaResource):
    """Represents a Kafka connector resource."""

    def __init__(self, kafka_connect_url: str, config: KafkaConnectorConfig) -> None:
        """
        Initialize Kafka connector.

        Args:
            kafka_connect_url: Kafka Connect URL
            config: Connector configuration
        """
        self.kafka_connect_url = kafka_connect_url
        self.config = config

    def exists(self) -> bool:
        """
        Check if connector exists.

        Returns:
            True if connector exists, False otherwise
        """
        try:
            response = requests.get(
                f"{self.kafka_connect_url}/connectors/" f"{self.config.name}", timeout=5
            )
            return response.status_code == 200
        except requests.RequestException:
            return False

    def create(self) -> bool:
        """
        Create connector, replacing if it already exists.

        Returns:
            True on success, False on failure
        """
        if not self.config.template:
            logger.error(f"Connector template not found: " f"{self.config.template}")
            return False

        logger.info(f"Processing connector: {self.config.name}")

        try:
            connector_config = self._load_and_substitute_template()

            if self.exists():
                logger.warning(f'Connector "{self.config.name}" already exists, ' "recreating...")
                if not self._delete():
                    return False

            return self._create(connector_config)

        except Exception as exception:
            logger.error(f'Error processing connector "{self.config.name}": ' f"{exception}")
            return False

    def _load_and_substitute_template(self) -> dict:
        """
        Load template and substitute environment variables.

        Returns:
            Parsed JSON configuration with substituted variables

        Raises:
            json.JSONDecodeError: If template is invalid JSON
        """
        import os

        with open(self.config.template, "r", encoding="utf-8") as file:
            template_text = file.read()

        substituted = Template(template_text).safe_substitute(os.environ)
        return json.loads(substituted)

    def _delete(self) -> bool:
        """
        Delete existing connector.

        Returns:
            True on success, False on failure
        """
        try:
            response = requests.delete(
                f"{self.kafka_connect_url}/connectors/" f"{self.config.name}", timeout=10
            )
            if response.status_code in [200, 204]:
                logger.info(f"Deleted connector: {self.config.name}")
                return True
            else:
                logger.error(
                    f'Failed to delete connector "{self.config.name}" '
                    f"(HTTP {response.status_code}): {response.text}"
                )
                return False
        except Exception as exception:
            logger.error(f'Error deleting connector "{self.config.name}": ' f"{exception}")
            return False

    def _create(self, connector_config: dict) -> bool:
        """
        Create new connector.

        Args:
            connector_config: Connector configuration dictionary

        Returns:
            True on success, False on failure
        """
        try:
            response = requests.post(
                f"{self.kafka_connect_url}/connectors",
                json=connector_config,
                headers={"Content-Type": "application/json"},
                timeout=10,
            )

            if response.status_code in [200, 201]:
                logger.info(f'✅ Connector "{self.config.name}" is ready')
                return True
            else:
                logger.error(
                    f'Failed to create connector "{self.config.name}" '
                    f"(HTTP {response.status_code}): {response.text}"
                )
                return False
        except Exception as exception:
            logger.error(f'Error creating connector "{self.config.name}": ' f"{exception}")
            return False
