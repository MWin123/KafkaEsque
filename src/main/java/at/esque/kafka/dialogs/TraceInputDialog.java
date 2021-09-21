package at.esque.kafka.dialogs;

import at.esque.kafka.Main;
import at.esque.kafka.alerts.ErrorAlert;
import at.esque.kafka.controls.InstantPicker;
import at.esque.kafka.handlers.Settings;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.*;
import java.util.*;

public class TraceInputDialog {
    public static Optional<TraceInput> show(boolean isKeyTrace, boolean isAvroKeyType, boolean traceQuickSelectEnabled, List<Duration> durations) {
        Dialog<TraceInput> dialog = new Dialog<>();
        Main.applyIcon(dialog);
        Main.applyStylesheet(dialog.getDialogPane().getScene());
        if (isKeyTrace) {
            dialog.setTitle("Trace Key");
            dialog.setHeaderText("Input Key to trace");
        } else {
            dialog.setTitle("Trace in Value");
            dialog.setHeaderText("Input regex to trace");
        }

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 20, 20));

        TextField key = new TextField();
        key.setPrefWidth(500);
        CheckBox fastTraceFlag = new CheckBox();
        HBox hBox = new HBox();
        InstantPicker instantPicker = new InstantPicker();
        instantPicker.setInstantValue(null);
        instantPicker.setMaxWidth(Double.MAX_VALUE);
        ToggleButton displayEpochMillis = new ToggleButton();
        displayEpochMillis.setGraphic(new FontIcon("fa-exchange"));
        displayEpochMillis.setMinHeight(30);
        instantPicker.displayAsEpochProperty().bind(displayEpochMillis.selectedProperty());
        HBox.setHgrow(instantPicker, Priority.ALWAYS);
        hBox.getChildren().addAll(instantPicker, displayEpochMillis);
        Label startTimeLabel = new Label("Start Epoch Timestamp");

        if (isKeyTrace) {
            if (!isAvroKeyType) {
                Label fastTraceLabel = new Label("Use fast trace:");
                fastTraceLabel.setTooltip(new Tooltip("Fast Trace traces in one partition determined by the default partitioning"));
                grid.add(fastTraceLabel, 0, 1);
                grid.add(fastTraceFlag, 1, 1);
            }
            key.setPromptText("search");
            grid.add(new Label("Key:"), 0, 0);
        } else {
            key.setPromptText("regex");
            grid.add(new Label("regex:"), 0, 0);
        }
        grid.add(key, 1, 0);
        grid.add(startTimeLabel, 0, 2);
        grid.add(hBox, 1, 2);

        if(traceQuickSelectEnabled) {
            HBox buttonBar = new HBox();
            fillButtonBar(buttonBar, instantPicker, durations);
            buttonBar.setMaxWidth(Double.MAX_VALUE);
            buttonBar.setAlignment(Pos.CENTER);
            HBox.setHgrow(buttonBar, Priority.ALWAYS);
            grid.add(buttonBar, 1, 3);
        }

        dialog.getDialogPane().setContent(grid);

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.getStyleClass().add("primary");

        Platform.runLater(key::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new TraceInput(key.getText(), fastTraceFlag.isSelected(), instantPicker.getInstantValue() == null ? null : instantPicker.getInstantValue().toEpochMilli());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static void fillButtonBar(HBox buttonBar, InstantPicker instantPicker, List<Duration> durations) {
        Button todayButton = new Button("Today");
        todayButton.setOnAction(event -> {
            OffsetDateTime offsetDateTime = Instant.now().atOffset(ZoneOffset.UTC);
            Instant today = OffsetDateTime.of(offsetDateTime.toLocalDate(), LocalTime.of(0, 0, 0, 0), ZoneOffset.UTC).toInstant();
            instantPicker.setInstantValue(today);
        });
        buttonBar.getChildren().add(todayButton);
        durations.forEach(duration -> {
            Period period = Period.ofDays((int) duration.toDays());
            Button button = new Button("Now - " + stringifyDuration(duration));
            button.setOnAction(event -> {
                try {
                    instantPicker.setInstantValue(Instant.now().minus(duration));
                } catch (Exception e) {
                    ErrorAlert.show(e);
                }
            });
            buttonBar.getChildren().add(button);
        });
    }

    public static class TraceInput {

        private String search;
        private boolean fastTrace;
        private Long epoch;

        public TraceInput(String key, boolean fastTrace, Long epoch) {
            this.search = key;
            this.fastTrace = fastTrace;
            this.epoch = epoch;
        }

        public String getSearch() {
            return search;
        }

        public void setSearch(String search) {
            this.search = search;
        }

        public boolean isFastTrace() {
            return fastTrace;
        }

        public void setFastTrace(boolean fastTrace) {
            this.fastTrace = fastTrace;
        }

        public Long getEpoch() {
            return epoch;
        }

        public void setEpoch(Long epoch) {
            this.epoch = epoch;
        }
    }

    private static String stringifyDuration(Duration duration) {
        StringBuilder builder = new StringBuilder("");
        long days = duration.toDays();
        long hours = duration.minusDays(days).toHours();
        long minutes = duration.minusDays(days).minusHours(hours).toMinutes();
        long seconds = duration.minusDays(days).minusHours(hours).minusMinutes(minutes).getSeconds();
        if (days > 0) {
            builder.append(days)
                    .append(" days ");
        }
        if (hours > 0) {
            builder.append(hours)
                    .append(" hours ");
        }
        if (minutes > 0) {
            builder.append(minutes)
                    .append(" minutes ");
        }
        if (seconds > 0) {
            builder.append(seconds)
                    .append(" seconds ");
        }
        return builder.toString().trim();
    }
}
