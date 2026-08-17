class KafkaConnectorConfig:
    """Configuration for a Kafka connector."""

    def __init__(self, config_dict: dict) -> None:
        """
        Initialize connector configuration.

        Args:
            config_dict: Dictionary containing connector configuration
        """
        self.name = config_dict.get("name", "")
        self.template = config_dict.get("template", "")
