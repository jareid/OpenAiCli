package com.jareid.openaicli;

import com.jareid.openaicli.cli.CommandLineInterface;
import com.jareid.openaicli.ui.MainScreen;

import javax.swing.*;

/**
 * This class represents the Command Line Interface (CLI) to interact with the OpenAI GPT model.
 *
 * <p>The CLI provides a set of functionalities including:
 * <ul>
 *     <li>Read the history of the conversation from a file</li>
 *     <li>Write the history of the conversation to a file</li>
 *     <li>Generate a unique file name for the code file</li>
 *     <li>Check if a ChatMessage has code block</li>
 *     <li>Extract the code type and the code block from a ChatMessage</li>
 *     <li>Write the code block from a ChatMessage to a file</li>
 *     <li>Ask the GPT model and get the response</li>
 *     <li>Run the CLI</li>
 * </ul>
 *
 * <p>It can be run in two modes:
 * <ul>
 *     <li>CLI mode (default): it directly interacts with the OpenAI GPT model via the console</li>
 *     <li>UI mode: it runs a graphical user interface to interact with the OpenAI GPT model</li>
 * </ul>
 *
 * @author Jamie Reid
 * @version 0.0.2
 * @since 2023-07-08
 */
public class Main {
    /**
     * The application's entry point.
     *
     * <p> This main method sets up the CLI for interacting with the OpenAI GPT model,
     * then creates a new graphical user interface for the CLI and displays it.
     *
     * <p> The application defaults to CLI mode, unless the '--ui' or '-u' argument is passed,
     * in which case it runs in UI mode. The CLI mode can also be explicitly invoked by passing
     * the '--cli' or '-c' argument.
     *
     * @param args an array of command-line arguments for the application
     */
    public static void main(String[] args) {
        CommandLineInterface cli = new CommandLineInterface();
        boolean runInUIMode = false;

        for (String arg : args) {
            if (arg.equals("--ui") || arg.equals("-u")) {
                runInUIMode = true;
            } else if (arg.equals("--cli") || arg.equalsIgnoreCase("-c")) {
                runInUIMode = false;
            }
        }

        try {
            if (runInUIMode) {
                // Run the UI creation on the Event Dispatch Thread (EDT) for thread safety
                SwingUtilities.invokeLater(() -> new MainScreen( cli ));
            } else {
                cli.run();
            }
        } catch (RuntimeException runtimeException) {
            System.out.println( "Argggggh, you killed me because of the following reason: " + runtimeException.getMessage() );
            runtimeException.printStackTrace();
        }
    }

}
