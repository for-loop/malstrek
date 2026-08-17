from abc import ABC, abstractmethod


class KafkaResource(ABC):
    """Abstract base for Kafka resources (topics, schemas, connectors)."""

    @abstractmethod
    def create(self) -> bool:
        """
        Create or update the resource.

        Returns:
            True on success, False on failure
        """

    @abstractmethod
    def exists(self) -> bool:
        """
        Check if resource already exists.

        Returns:
            True if resource exists, False otherwise
        """
