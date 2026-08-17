import logging
from typing import Optional

from kafka.admin import KafkaAdminClient, NewTopic
from kafka.errors import TopicAlreadyExistsError

from scripts.resources.kafka_resource import KafkaResource
from scripts.config.kafka_topic_config import KafkaTopicConfig

logger = logging.getLogger(__name__)


class KafkaTopic(KafkaResource):
    """Represents a Kafka topic resource."""

    def __init__(self, broker: str, config: KafkaTopicConfig) -> None:
        """
        Initialize Kafka topic.

        Args:
            broker: Kafka broker address
            config: Topic configuration
        """
        self.broker = broker
        self.config = config

    def exists(self) -> bool:
        """
        Check if topic exists.

        Returns:
            True if topic exists, False otherwise
        """
        admin_client = None
        try:
            admin_client = KafkaAdminClient(bootstrap_servers=self.broker, request_timeout_ms=5000)
            metadata = admin_client.list_topics()
            return self.config.name in metadata
        except Exception:
            return False
        finally:
            if admin_client:
                admin_client.close()

    def create(self) -> bool:
        """
        Create topic if it does not exist.

        Returns:
            True on success, False on failure
        """
        if self.exists():
            logger.warning(f'Topic "{self.config.name}" already exists, skipping')
            return True

        logger.info(f"Creating topic: {self.config.name}")
        admin_client = None
        try:
            admin_client = KafkaAdminClient(bootstrap_servers=self.broker, request_timeout_ms=30000)

            new_topic = NewTopic(
                name=self.config.name,
                num_partitions=self.config.partitions,
                replication_factor=self.config.replication_factor,
            )

            result = admin_client.create_topics(new_topics=[new_topic], validate_only=False)

            result[self.config.name].result(timeout_sec=30)
            logger.info(f'✅ Topic "{self.config.name}" created')
            return True

        except TopicAlreadyExistsError:
            logger.warning(f'Topic "{self.config.name}" already exists')
            return True
        except Exception as exception:
            logger.error(f'Error creating topic "{self.config.name}": {exception}')
            return False
        finally:
            if admin_client:
                admin_client.close()
