package com.jareid.openaicli.javafxui;

import com.jareid.openaicli.cli.CommandLineInterface;
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

    private CommandLineInterface commandLineInterface;

    public ToolbarController(CommandLineInterface commandLineInterface) {
        this.commandLineInterface = commandLineInterface;
    }

    /**
     * Toggle the checked state of the sendHistoryCheckBox.
     */
    @FXML
    private void handleSendHistoryAction() {
        sendHistoryCheckBox.setSelected(!sendHistoryCheckBox.isSelected());
        commandLineInterface.changeOption( "disableSendingChatGPTHistory" );
    }

    /**
     * Toggle the checked state of the logHistoryCheckBox.
     */
    @FXML
    private void handleLogHistoryAction() {
        logHistoryCheckBox.setSelected( !logHistoryCheckBox.isSelected( ) );
        commandLineInterface.changeOption( "disableLoggingChatGPTHistory" );

        sendHistoryCheckBox.setSelected( !sendHistoryCheckBox.isSelected( ) );
        commandLineInterface.changeOption( "disableSendingChatGPTHistory" );
    }

    /**
     * Toggle the checked state of the outputCodeCheckBox.
     */
    @FXML
    private void handleOutputCodeAction() {
        outputCodeCheckBox.setSelected( !outputCodeCheckBox.isSelected( ) );
        commandLineInterface.changeOption( "disableOutputCodeToFile" );
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