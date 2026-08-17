class KafkaTopicConfig:
    """Configuration for a Kafka topic."""

    def __init__(self, config_dict: dict) -> None:
        """
        Initialize topic configuration.

        Args:
            config_dict: Dictionary containing topic configuration
        """
        self.name = config_dict.get("name", "")
        self.partitions = config_dict.get("partitions", 1)
        self.replication_factor = config_dict.get("replication_factor", 1)
