package at.esque.kafka;

import at.esque.kafka.alerts.ErrorAlert;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Function;

public class SystemUtils {

    public static void copyStringSelectionToClipboard(Callable<String> stringExtractor) {

        final ClipboardContent clipboardContent = new ClipboardContent();
        try {
            clipboardContent.putString(stringExtractor.call());
        } catch (Exception e) {
            ErrorAlert.show(e);
        }

        if (clipboardContent.getString() != null) {
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        }
    }

    public static Optional<String> showInputDialog(String defaultValue, String title, String header, String requestedInputLabel) {
        FutureTask<Optional<String>> futureTask = new FutureTask<>(() -> {
            TextInputDialog dialog = new TextInputDialog(defaultValue);
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(SystemUtils.class.getResource("/icons/kafkaesque.png").toString()));

            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);
            dialog.setHeaderText(header);
            dialog.setContentText(requestedInputLabel);
            Main.applyStylesheet(dialog.getDialogPane().getScene());

            return dialog.showAndWait();
        });
        Platform.runLater(futureTask);
        try {
            return futureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> EventHandler<? super KeyEvent> generateTableCopySelectedItemCopyEventHandler(TableView<T> targetTableView, Map<KeyCodeCombination, Function<T, String>> copyCombinationMap) {
        return keyEvent -> {
            if (targetTableView.equals(keyEvent.getSource()) && targetTableView.getSelectionModel().getSelectedItem() != null) {
                copyCombinationMap.entrySet()
                        .stream()
                        .filter(keyCombinationFunctionEntry -> keyCombinationFunctionEntry.getKey().match(keyEvent))
                        .findFirst()
                        .ifPresent(keyCombinationFunctionEntry -> SystemUtils.copyStringSelectionToClipboard(() -> keyCombinationFunctionEntry.getValue().apply(targetTableView.getSelectionModel().getSelectedItem())));
            }
        };
    }
}
