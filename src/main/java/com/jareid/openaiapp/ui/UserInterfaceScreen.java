package com.jareid.openaiapp.ui;

import com.jareid.openaiapp.api.APIHandler;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;

import javax.swing.*;
import java.awt.*;



/**
 * The {@code MainScreen} class provides a simple graphical user interface for interacting with
 * an OpenAI GPT model through the {@link APIHandler} class.
 *
 * <p> The interface contains two main components:
 * 1. A text field for the user to enter their input.
 * 2. A text area to display the output from the OpenAI GPT model.
 *
 * <p> A "Send" button is used to submit the user's input to the model.
 *
 * <p> This class is a basic example and does not follow the Model-View-Controller (MVC) design pattern.
 * In a more complex application, following the MVC pattern would be recommended.
 *
 * @author Jamie Reid
 * @version 0.0.2
 * @since 2023-07-11
 */
public class UserInterfaceScreen {
    private final String DEFAULT_ERROR = "An application error occurred....";

    private final JTextField userInputField;
    private final JEditorPane outputArea;

    /**
     * Constructs a new MainScreen object with the specified CLI.
     *
     * @param cli The command line interface for interacting with the OpenAI GPT model.
     */
    public UserInterfaceScreen(APIHandler cli) {

        JFrame frame = new JFrame("OpenAI CLI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // Initialize Toolbar
        JToolBar toolBar = new JToolBar();
        JCheckBox sendHistoryCheckBox = new JCheckBox("Send History");
        toolBar.add(sendHistoryCheckBox);
        sendHistoryCheckBox.setSelected(true);
        sendHistoryCheckBox.addActionListener(e -> {
            if (cli == null) {
                showErrorDialog(DEFAULT_ERROR);
            } else {
                cli.changeOption("disableSendingChatGPTHistory");
            }
        });
        toolBar.add(sendHistoryCheckBox);

        JCheckBox logHistoryCheckBox = new JCheckBox("Log History");
        logHistoryCheckBox.setSelected(true);
        logHistoryCheckBox.addActionListener(e -> {
            if (cli == null) {
                showErrorDialog(DEFAULT_ERROR);
            } else {
                cli.changeOption("disableLoggingChatGPTHistory");
            }
        });
        toolBar.add(logHistoryCheckBox);

        JCheckBox outputCodeCheckBox = new JCheckBox("Output Code");
        outputCodeCheckBox.setSelected(true);
        outputCodeCheckBox.addActionListener(e -> {
            if (cli == null) {
                showErrorDialog(DEFAULT_ERROR);
            } else {
                cli.changeOption("disableOutputCodeToFile");
            }
        });
        toolBar.add(outputCodeCheckBox);

        JButton aboutButton = new JButton("About");
        aboutButton.addActionListener(e -> JOptionPane.showMessageDialog(frame,
                """
                        This is an application for interacting with an OpenAI GPT model.
                        Author: Jamie Reid
                        Version: 0.0.2""",
                "About",
                JOptionPane.INFORMATION_MESSAGE));
        toolBar.add(aboutButton);

        // User input field
        userInputField = new JTextField();
        userInputField.setPreferredSize(new Dimension(400, 30));

        // Top panel to hold the toolbar and user input field
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(toolBar);
        topPanel.add(userInputField);

        // Output area
        outputArea = new JEditorPane();
        outputArea.setContentType("text/html");
        outputArea.setEditable(false);

        /*
         * The SendButtonListener class listens for the 'Send' button to be pressed,
         * at which point it takes the user's input and sends it to the OpenAI GPT model.
         */
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            String userInput = userInputField.getText(); // Get User's input
            try {
                ChatMessage response = cli.askGPT_GetResponse(userInput); // Pass user input to OpenAI

                // Generate our response using HTML <br> tags for new lines
                String output = "**You:** " + userInput + "<br><br>" +
                        "**OpenAI:** " + response.getContent() + "<br>";

                // Convert Markdown to HTML using Flexmark
                MutableDataSet options = new MutableDataSet();
                String document = FlexmarkHtmlConverter.builder(options).build().convert(output);

                // Set HTML content
                outputArea.setText( document );
                userInputField.setText(""); // Wipe user input
            } catch (Exception exception) {
                showErrorDialog("Error with the ChatGPT API occurred: " + exception.getMessage() );
            }
        });


        panel.add(topPanel, BorderLayout.PAGE_START);   // Changed from toolBar to topPanel
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }


    private void showErrorDialog( String errorMessage ) {
        JOptionPane.showMessageDialog(null,
                                       errorMessage,
                                      "Error",
                                       JOptionPane.ERROR_MESSAGE);
    }


}
