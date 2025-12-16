package org.studioapriori.malstrek.services;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.studioapriori.malstrek.model.RaceEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RaceEventProducerTest {
    private static final String TOPIC = "test-topic";
    private static final long TIMESTAMP = 1762883020000L;
    private static final int RACE_NUMBER = 2028;
    private static final String JSON_STRING = "{\"raceNumber\":2028}";

    @Mock
    private Producer<String, String> mockProducer;

    @Mock
    private EventCallback mockCallback;

    private RaceEventProducer producer;

    @BeforeEach
    void setUp() {
        producer = new RaceEventProducer(mockProducer);
    }

    private RaceEvent createTestEvent() {
        return new RaceEvent(TOPIC, TIMESTAMP, RACE_NUMBER, JSON_STRING);
    }

    private RaceEvent createTestEvent(long timestamp) {
        return new RaceEvent(TOPIC, timestamp, RACE_NUMBER, JSON_STRING);
    }

    @Test
    void sendEvent_createsProducerRecordWithCorrectTopic() {
        producer.sendEvent(createTestEvent(), mockCallback);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(captor.capture(), any(Callback.class));

        ProducerRecord<String, String> record = captor.getValue();
        assertEquals(TOPIC, record.topic());
    }

    @Test
    void sendEvent_createsProducerRecordWithCorrectTimestamp() {
        producer.sendEvent(createTestEvent(), mockCallback);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(captor.capture(), any(Callback.class));

        ProducerRecord<String, String> record = captor.getValue();
        assertEquals(TIMESTAMP, record.timestamp());
    }

    @Test
    void sendEvent_createsProducerRecordWithCorrectValue() {
        producer.sendEvent(createTestEvent(), mockCallback);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(captor.capture(), any(Callback.class));

        ProducerRecord<String, String> record = captor.getValue();
        assertEquals(JSON_STRING, record.value());
    }

    @Test
    void sendEvent_createsProducerRecordWithNullKey() {
        producer.sendEvent(createTestEvent(), mockCallback);

        ArgumentCaptor<ProducerRecord<String, String>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(mockProducer).send(captor.capture(), any(Callback.class));

        ProducerRecord<String, String> record = captor.getValue();
        assertNull(record.key());
    }

    @Test
    void sendEvent_callsProducerSendMethod() {
        producer.sendEvent(createTestEvent(), mockCallback);

        verify(mockProducer).send(any(ProducerRecord.class), any(Callback.class));
    }

    @Test
    void sendEvent_onSuccess_invokesCallbackWithCorrectParameters() {
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        
        producer.sendEvent(createTestEvent(), mockCallback);
        
        verify(mockProducer).send(any(ProducerRecord.class), callbackCaptor.capture());
        
        // Simulate successful send (null exception)
        callbackCaptor.getValue().onCompletion(null, null);
        
        verify(mockCallback).onSuccess(createTestEvent());
    }

    @Test
    void sendEvent_onError_invokesErrorCallback() {
        ArgumentCaptor<Callback> callbackCaptor = ArgumentCaptor.forClass(Callback.class);
        
        producer.sendEvent(createTestEvent(), mockCallback);
        
        verify(mockProducer).send(any(ProducerRecord.class), callbackCaptor.capture());
        
        // Simulate error (non-null exception)
        Exception testException = new RuntimeException("Test error");
        callbackCaptor.getValue().onCompletion(null, testException);
        
        verify(mockCallback).onError(testException);
    }

    @Test
    void close_closesProducer() {
        producer.close();

        verify(mockProducer).close();
    }

    @Test
    void create_returnsValidProducerInstance() {
        RaceEventProducer createdProducer = RaceEventProducer.create("localhost:9092");

        assertNotNull(createdProducer);
        
        createdProducer.close();
    }

    @Test
    void sendEvent_withMultipleEvents_sendsAllCorrectly() {
        producer.sendEvent(createTestEvent(), mockCallback);
        producer.sendEvent(createTestEvent(TIMESTAMP + 1000), mockCallback);

        verify(mockProducer, times(2)).send(any(ProducerRecord.class), any(Callback.class));
    }
}
