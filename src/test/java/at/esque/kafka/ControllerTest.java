package at.esque.kafka;

import at.esque.kafka.topics.model.KafkaMessageBookWrapper;
import at.esque.kafka.topics.model.KafkaMessageForMessageBook;
import org.junit.Test;
import static org.junit.Assert.*;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ControllerTest {

    @Test
    public void testAddMessagesToSend() {
        // Given
        List<KafkaMessageBookWrapper> messagesToSend = new ArrayList<>();
        File playFile = new File("src/test/resources/csv_testfiles/testAddMessagesToSend.csv");

        KafkaMessageForMessageBook message1Expected = new KafkaMessageForMessageBook();
        message1Expected.setKey("${value1:UUID}");
        message1Expected.setPartition(-1);
        message1Expected.setTimestamp("2022-11-06T17:11:52.919Z");
        message1Expected.setValue("{\"version\":0,\"newId\":\"${value1:UUID}\"}");

        KafkaMessageForMessageBook message2Expected = new KafkaMessageForMessageBook();
        message2Expected.setKey("${value2:UUID}");
        message2Expected.setPartition(-1);
        message2Expected.setTimestamp("2022-11-06T17:13:52.919Z");
        message2Expected.setValue("{\"version\":0,\"newId\":\"${value2:UUID}\"}");

        // When
        Controller controller = new Controller();
        controller.addMessagesToSend(messagesToSend, playFile);

        // Then
        assertEquals(2, messagesToSend.size());

        assertEquals(message1Expected.getPartition(), messagesToSend.get(0).getWrappedMessage().getPartition());
        assertEquals(message1Expected.getKey(), messagesToSend.get(0).getWrappedMessage().getKey());
        assertEquals(message1Expected.getTimestamp(), messagesToSend.get(0).getWrappedMessage().getTimestamp());
        assertEquals(message1Expected.getValue(), messagesToSend.get(0).getWrappedMessage().getValue());

        assertEquals(message2Expected.getPartition(), messagesToSend.get(1).getWrappedMessage().getPartition());
        assertEquals(message2Expected.getKey(), messagesToSend.get(1).getWrappedMessage().getKey());
        assertEquals(message2Expected.getTimestamp(), messagesToSend.get(1).getWrappedMessage().getTimestamp());
        assertEquals(message2Expected.getValue(), messagesToSend.get(1).getWrappedMessage().getValue());
    }
}