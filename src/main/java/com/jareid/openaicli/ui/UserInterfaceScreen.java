package com.jareid.openaicli.ui;

import com.jareid.openaicli.cli.CommandLineInterface;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.util.data.MutableDataSet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The {@code MainScreen} class provides a simple graphical user interface for interacting with
 * an OpenAI GPT model through the {@link CommandLineInterface} class.
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
    private final CommandLineInterface cli;
    private final JTextField userInputField;
    private final JEditorPane outputArea;

    /**
     * Constructs a new MainScreen object with the specified CLI.
     *
     * @param cli The command line interface for interacting with the OpenAI GPT model.
     */
    public UserInterfaceScreen(CommandLineInterface cli) {
        this.cli = cli;

        JFrame frame = new JFrame("OpenAI CLI");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // User input field
        userInputField = new JTextField();

        // Output area
        outputArea = new JEditorPane();  // Changed to JEditorPane
        outputArea.setContentType("text/html");  // Set content type to HTML
        outputArea.setEditable(false);

        // Send button
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new SendButtonListener());

        panel.add(userInputField, BorderLayout.NORTH);
        panel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    /**
     * The SendButtonListener class listens for the 'Send' button to be pressed,
     * at which point it takes the user's input and sends it to the OpenAI GPT model.
     */
    private class SendButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userInput = userInputField.getText(); // Get User's input

            ChatMessage response = cli.askGPT_GetResponse( userInput ); // Pass user input to OpenAI

            // Generate our response
            String output = "<b>You: </b>" + userInput + System.lineSeparator() +
                            System.lineSeparator() +
                            "<b>OpenAI: </b>" + response.getContent() + System.lineSeparator();

            // Convert Markdown to HTML using flexmark
            MutableDataSet options = new MutableDataSet();
            String document = FlexmarkHtmlConverter.builder( options ).build( ).convert( output );

            // Set HTML content
            String html = "<html><body>" + document + "</body></html>";
            outputArea.setText( html );

            userInputField.setText(""); // Wipe user input
        }
    }
}
