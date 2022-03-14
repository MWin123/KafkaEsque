package at.esque.kafka.controls;

import at.esque.kafka.topics.KafkaMessage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;

public class KafkaMessageTableView extends TableView<KafkaMessage> {
    private ObservableList<KafkaMessage> baseList;
    private FilteredList<KafkaMessage> filteredMessages;
    private SortedList<KafkaMessage> sortedMessages;

    public KafkaMessageTableView() {
        this(FXCollections.observableArrayList());
    }

    public KafkaMessageTableView(ObservableList<KafkaMessage> baseList) {
        super();
        this.baseList = baseList;
        filteredMessages = new FilteredList<>(baseList, km -> true);
        sortedMessages = new SortedList<>(filteredMessages);
        sortedMessages.comparatorProperty().bind(this.comparatorProperty());
        buildTableColumns();
        this.setItems(sortedMessages);
        this.minHeight(0);
        this.minWidth(0);
    }

    private void buildTableColumns() {
        TableColumn<KafkaMessage, Long> messageOffsetColumn = new TableColumn<>("Offset");
        messageOffsetColumn.setCellValueFactory(new PropertyValueFactory<>("offset"));

        TableColumn<KafkaMessage, Integer> messagePartitionColumn = new TableColumn<>("Partition");
        messagePartitionColumn.setCellValueFactory(new PropertyValueFactory<>("partition"));

        TableColumn<KafkaMessage, String> messageTimestampColumn = new TableColumn<>("Timestamp");
        messageTimestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        TableColumn<KafkaMessage, String> messageKeyColumn = new TableColumn<>("Key");
        messageKeyColumn.setCellValueFactory(param -> {
            if (param.getValue() != null && param.getValue().getKey() != null) {
                return new SimpleStringProperty(param.getValue().getKey().replaceAll("\\r\\n|\\r|\\n", " "));
            } else {
                return null;
            }
        });

        messageKeyColumn.setCellFactory(param -> getNullMarkingTableCell("NULL", Color.GRAY));

        TableColumn<KafkaMessage, String> messageValueColumn = new TableColumn<>("Value");
        messageValueColumn.setCellValueFactory(param -> {
            if (param.getValue() != null && param.getValue().getValue() != null) {
                return new SimpleStringProperty(param.getValue().getValue().replaceAll("\\r\\n|\\r|\\n", " "));
            } else {
                return null;
            }
        });
        messageValueColumn.setCellFactory(param -> getNullMarkingTableCell("NULL/Tombstone", Color.GRAY));
        this.getColumns().addAll(messageTimestampColumn, messagePartitionColumn, messageOffsetColumn, messageKeyColumn, messageValueColumn);
    }

    @NotNull
    private TableCell<KafkaMessage, String> getNullMarkingTableCell(String nullDisplayString, Color nullDisplayColor) {
        return new TableCell<KafkaMessage, String>() {
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null && !empty) {
                    setTextFill(nullDisplayColor);
                    setText(nullDisplayString);
                } else {
                    setTextFill(Color.BLACK);
                    setText(item);
                }
            }
        };
    }

    public ObservableList<KafkaMessage> getBaseList() {
        return baseList;
    }

    public FilteredList<KafkaMessage> getFilteredMessages() {
        return filteredMessages;
    }

    public SortedList<KafkaMessage> getSortedMessages() {
        return sortedMessages;
    }
}
