class KafkaSchemaConfig:
    """Configuration for a Kafka schema."""

    def __init__(self, config_dict: dict) -> None:
        """
        Initialize schema configuration.

        Args:
            config_dict: Dictionary containing schema configuration
        """
        self.subject = config_dict.get("subject", "")
        self.file = config_dict.get("file", "")
