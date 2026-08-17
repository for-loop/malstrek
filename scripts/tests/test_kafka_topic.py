from unittest.mock import patch, MagicMock
import pytest

from kafka.errors import TopicAlreadyExistsError

from scripts.resources.kafka_topic import KafkaTopic
from scripts.config.kafka_topic_config import KafkaTopicConfig


class TestKafkaTopic:
    """Test suite for KafkaTopic."""

    @pytest.fixture
    def topic_config(self) -> KafkaTopicConfig:
        """Create a test topic configuration."""
        return KafkaTopicConfig({"name": "test-topic", "partitions": 1, "replication_factor": 1})

    @pytest.fixture
    def kafka_topic(self, topic_config: KafkaTopicConfig) -> KafkaTopic:
        """Create a test KafkaTopic instance."""
        return KafkaTopic(broker="localhost:9092", config=topic_config)

    @pytest.mark.parametrize(
        "list_topics_return, list_topics_side_effect, expected_result",
        [
            (["test-topic", "other-topic"], None, True),  # Topic exists
            (["other-topic"], None, False),  # Topic missing
            (None, Exception("Connection failed"), False),  # Network exception
        ],
    )
    @patch("scripts.resources.kafka_topic.KafkaAdminClient")
    def test_exists_scenarios(
        self,
        mock_admin_client_class: MagicMock,
        kafka_topic: KafkaTopic,
        list_topics_return: list | None,  # Uses clean Python 3.10+ pipe syntax
        list_topics_side_effect: Exception | None,  # Uses clean Python 3.10+ pipe syntax
        expected_result: bool,
    ) -> None:
        """Test variations of the exists() check."""
        mock_admin_instance = MagicMock()
        mock_admin_client_class.return_value = mock_admin_instance

        if list_topics_side_effect:
            mock_admin_instance.list_topics.side_effect = list_topics_side_effect
        else:
            mock_admin_instance.list_topics.return_value = list_topics_return

        assert kafka_topic.exists() is expected_result
        mock_admin_instance.close.assert_called_once()

    @patch("scripts.resources.kafka_topic.KafkaTopic.exists")
    def test_create_skips_existing_topic(
        self, mock_exists: MagicMock, kafka_topic: KafkaTopic
    ) -> None:
        """Test that create() skips if topic already exists."""
        mock_exists.return_value = True

        assert kafka_topic.create() is True
        mock_exists.assert_called_once()

    @pytest.mark.parametrize(
        "result_side_effect, expected_result",
        [
            (None, True),  # Success path
            (Exception("Topic creation failed"), False),  # Generic failure
            (TopicAlreadyExistsError("Topic already exists"), True),  # Existing cluster topic error
        ],
    )
    @patch("scripts.resources.kafka_topic.KafkaAdminClient")
    def test_create_scenarios(
        self,
        mock_admin_client_class: MagicMock,
        kafka_topic: KafkaTopic,
        result_side_effect: Exception | None,  # Uses clean Python 3.10+ pipe syntax
        expected_result: bool,
    ) -> None:
        """Test variations of the create() method execution paths."""
        with patch.object(kafka_topic, "exists", return_value=False):
            mock_admin_instance = MagicMock()
            mock_admin_client_class.return_value = mock_admin_instance

            mock_result_future = MagicMock()
            if result_side_effect:
                mock_result_future.result.side_effect = result_side_effect
            else:
                mock_result_future.result.return_value = None

            mock_admin_instance.create_topics.return_value = {"test-topic": mock_result_future}

            assert kafka_topic.create() is expected_result
            mock_admin_instance.create_topics.assert_called_once()
            mock_admin_instance.close.assert_called_once()
