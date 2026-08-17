import socket
import sys
import time
import logging
from typing import Callable

import requests

logger = logging.getLogger(__name__)


class ServiceMonitor:
    """Monitors health of Kafka infrastructure services."""

    def __init__(
        self,
        kafka_broker: str,
        schema_registry_url: str,
        kafka_connect_url: str,
        max_retries: int = 30,
        retry_delay: int = 2,
    ) -> None:
        """
        Initialize service monitor.

        Args:
            kafka_broker: Kafka broker address
            schema_registry_url: Schema Registry URL
            kafka_connect_url: Kafka Connect URL
            max_retries: Maximum retry attempts
            retry_delay: Delay between retries in seconds
        """
        self.kafka_broker = kafka_broker
        self.schema_registry_url = schema_registry_url
        self.kafka_connect_url = kafka_connect_url
        self.max_retries = max_retries
        self.retry_delay = retry_delay

    def wait_for_service(self, service_name: str, check_func: Callable[[], bool]) -> bool:
        """
        Wait for service to become healthy.

        Args:
            service_name: Service name for logging
            check_func: Function that returns True when service is ready

        Returns:
            True if service became ready, False if max retries exceeded
        """
        logger.info(f"Waiting for {service_name}...")

        for attempt in range(1, self.max_retries + 1):
            try:
                if check_func():
                    logger.info(f"✅ {service_name} is ready")
                    return True
            except Exception:
                pass

            if attempt < self.max_retries:
                sys.stdout.write(f"\r  Attempt {attempt}/{self.max_retries}...")
                sys.stdout.flush()
                time.sleep(self.retry_delay)

        logger.error(
            f"{service_name} did not become ready after "
            f"{self.max_retries * self.retry_delay} seconds"
        )
        return False

    def wait_for_broker(self) -> bool:
        """
        Wait for Kafka broker to be reachable.

        Returns:
            True if broker is ready, False on timeout
        """
        host, port_str = self.kafka_broker.rsplit(":", 1)
        port = int(port_str)

        def check_broker() -> bool:
            try:
                with socket.create_connection((host, port), timeout=2) as socket_connection:
                    return True
            except socket.timeout, ConnectionRefusedError:
                return False

        return self.wait_for_service("Kafka Broker", check_broker)

    def wait_for_schema_registry(self) -> bool:
        """
        Wait for Schema Registry to be reachable.

        Returns:
            True if Schema Registry is ready, False on timeout
        """

        def check_registry() -> bool:
            try:
                response = requests.get(f"{self.schema_registry_url}/subjects", timeout=2)
                return response.status_code == 200
            except requests.RequestException:
                return False

        return self.wait_for_service("Schema Registry", check_registry)

    def wait_for_kafka_connect(self) -> bool:
        """
        Wait for Kafka Connect to be reachable.

        Returns:
            True if Kafka Connect is ready, False on timeout
        """

        def check_connect() -> bool:
            try:
                response = requests.get(f"{self.kafka_connect_url}/health", timeout=2)
                return response.status_code == 200
            except requests.RequestException:
                return False

        return self.wait_for_service("Kafka Connect", check_connect)
