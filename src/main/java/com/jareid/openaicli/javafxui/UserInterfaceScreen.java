package com.jareid.openaicli.javafxui;

import com.jareid.openaicli.cli.CommandLineInterface;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * UserInterfaceScreen is the main JavaFX controller class for OpenAI CLI interface.
 *
 * @author Jamie Reid
 * @version 0.0.2
 * @since 2023-07-11
 */
public class UserInterfaceScreen {
    private final CommandLineInterface cli;

    @FXML
    private ToolbarController toolbarController;
    @FXML
    private TextField userInputField;

    @FXML
    private WebView outputArea;

    private Stage primaryStage;

    /**
     * Constructs a new UserInterfaceScreen object with the specified CLI.
     *
     * @param cli The command line interface for interacting with the OpenAI GPT model.
     */
    public UserInterfaceScreen(CommandLineInterface cli) {
        this.cli = cli;
    }

    /**
     * Shows the main screen.
     *
     * @throws IOException If an error occurs during loading the FXML file.
     */
    public void show() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("UserInterfaceScreen.fxml"));
        loader.setController(this);
        primaryStage.setScene(new Scene(loader.load(), 500, 400));
        primaryStage.setTitle("OpenAI CLI");
        primaryStage.show();
    }

    /**
     * Sets the primary stage for the screen.
     *
     * @param primaryStage The primary stage.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Handles "Send" action from the main screen.
     * Takes the user's input, sends it to the OpenAI GPT model, gets the response and shows it in the output area.
     *
     * @param event The action event.
     */
    @FXML
    private void handleSendAction(ActionEvent event) {
        String userInput = userInputField.getText(); // Get User's input

        ChatMessage response = cli.askGPT_GetResponse(userInput); // Pass user input to OpenAI

        // Generate our response
        String output = "<b>You: </b>" + userInput + System.lineSeparator() +
                        System.lineSeparator() +
                        "<b>OpenAI: </b>" + response.getContent() + System.lineSeparator();

        // Convert Markdown to HTML using flexmark
        MutableDataSet options = new MutableDataSet();
        String document = FlexmarkHtmlConverter.builder(options).build().convert(output);

        // Set HTML content
        String html = "<html><body>" + document + "</body></html>";
        outputArea.getEngine().loadContent(html);

        userInputField.setText(""); // Wipe user input
    }
}