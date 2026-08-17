from unittest.mock import patch, MagicMock
import socket
import pytest
import requests

from scripts.services.service_monitor import ServiceMonitor


class TestServiceMonitor:
    """Test suite for ServiceMonitor."""

    @pytest.fixture
    def service_monitor(self) -> ServiceMonitor:
        """Create a test ServiceMonitor instance."""
        return ServiceMonitor(
            kafka_broker="localhost:9092",
            schema_registry_url="http://localhost:8081",
            kafka_connect_url="http://localhost:8083",
            max_retries=3,
            retry_delay=0,
        )

    def test_wait_for_service_returns_true_immediately(
        self, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_service returns True if check passes."""
        check_func = MagicMock(return_value=True)
        assert service_monitor.wait_for_service("TestService", check_func)

    def test_wait_for_service_retries_on_failure(self, service_monitor: ServiceMonitor) -> None:
        """Test wait_for_service retries on failure."""
        check_func = MagicMock(side_effect=[False, False, True])
        assert service_monitor.wait_for_service("TestService", check_func)
        assert check_func.call_count == 3

    def test_wait_for_service_returns_false_on_max_retries(
        self, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_service returns False after max retries."""
        check_func = MagicMock(return_value=False)
        assert not service_monitor.wait_for_service("TestService", check_func)
        assert check_func.call_count == 3

    @patch("socket.create_connection")
    def test_wait_for_broker_returns_true_on_connect(
        self, mock_socket: MagicMock, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_broker returns True on successful connection."""
        mock_socket.return_value.__enter__ = MagicMock()
        mock_socket.return_value.__exit__ = MagicMock()
        assert service_monitor.wait_for_broker()

    @patch("socket.create_connection")
    def test_wait_for_broker_returns_false_on_timeout(
        self, mock_socket: MagicMock, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_broker returns False on connection timeout."""
        mock_socket.side_effect = socket.timeout
        assert not service_monitor.wait_for_broker()

    @patch("requests.get")
    def test_wait_for_schema_registry_returns_true_on_success(
        self, mock_get: MagicMock, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_schema_registry returns True on 200 response."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_get.return_value = mock_response
        assert service_monitor.wait_for_schema_registry()

    @patch("requests.get")
    def test_wait_for_kafka_connect_returns_true_on_success(
        self, mock_get: MagicMock, service_monitor: ServiceMonitor
    ) -> None:
        """Test wait_for_kafka_connect returns True on 200 response."""
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_get.return_value = mock_response
        assert service_monitor.wait_for_kafka_connect()
