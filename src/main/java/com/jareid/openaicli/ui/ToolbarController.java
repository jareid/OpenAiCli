package com.jareid.openaicli.ui;

import com.jareid.openaicli.api.APIHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * ToolbarController is a JavaFX controller class that handles events from toolbar components such as checkboxes and "About" action.
 *
 * @author Jamie Reid
 * @version 0.0.2
 * @since 2023-07-11
 */
public class ToolbarController {
    @FXML
    private CheckBox sendHistoryCheckBox;

    @FXML
    private CheckBox logHistoryCheckBox;

    @FXML
    private CheckBox outputCodeCheckBox;

    private APIHandler APIHandler;

    public ToolbarController(APIHandler APIHandler) {
        this.APIHandler = APIHandler;
    }

    /**
     * Toggle the checked state of the sendHistoryCheckBox.
     */
    @FXML
    private void handleSendHistoryAction() {
        sendHistoryCheckBox.setSelected(!sendHistoryCheckBox.isSelected());
        APIHandler.changeOption( "disableSendingChatGPTHistory" );
    }

    /**
     * Toggle the checked state of the logHistoryCheckBox.
     */
    @FXML
    private void handleLogHistoryAction() {
        logHistoryCheckBox.setSelected( !logHistoryCheckBox.isSelected( ) );
        APIHandler.changeOption( "disableLoggingChatGPTHistory" );

        sendHistoryCheckBox.setSelected( !sendHistoryCheckBox.isSelected( ) );
        APIHandler.changeOption( "disableSendingChatGPTHistory" );
    }

    /**
     * Toggle the checked state of the outputCodeCheckBox.
     */
    @FXML
    private void handleOutputCodeAction() {
        outputCodeCheckBox.setSelected( !outputCodeCheckBox.isSelected( ) );
        APIHandler.changeOption( "disableOutputCodeToFile" );
    }

    /**
     * Handle "About" action from toolbar.
     * Shows a dialog with application's information.
     */
    @FXML
    private void handleAboutAction() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText(null);
        alert.setContentText("This is an application for interacting with an OpenAI GPT model.\n" +
                "Author: Jamie Reid\nVersion: 0.0.2");

        alert.showAndWait();
    }
}