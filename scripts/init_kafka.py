import os
import sys
import logging
from pathlib import Path

# Add parent directory to Python path so we can import scripts module
sys.path.insert(0, str(Path(__file__).parent.parent))

from scripts.kafka_initializer import KafkaInitializer

logging.basicConfig(level=logging.INFO, format="%(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


def main() -> int:
    """
    Entry point for the initialization script.

    Returns:
        Exit code (0 on success, 1 on failure)
    """
    try:
        kafka_broker = os.getenv("KAFKA_BOOTSTRAP_SERVERS", "broker:29092")
        schema_registry_url = os.getenv("KAFKA_SCHEMA_REGISTRY_URL", "http://schema-registry:8081")
        kafka_connect_url = os.getenv("KAFKA_CONNECT_URL", "http://connect:8083")

        initializer = KafkaInitializer(
            kafka_broker=kafka_broker,
            schema_registry_url=schema_registry_url,
            kafka_connect_url=kafka_connect_url,
        )
        success = initializer.run()
        return 0 if success else 1
    except Exception as exception:
        logger.error(f"Unexpected error: {exception}", exc_info=True)
        return 1


if __name__ == "__main__":
    sys.exit(main())
