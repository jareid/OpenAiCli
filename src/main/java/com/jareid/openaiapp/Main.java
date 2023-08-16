package com.jareid.openaiapp;

import com.jareid.openaiapp.api.APIHandler;
import com.jareid.openaiapp.ui.UserInterfaceScreen;

/**
 * The {@code MainJavaFX} class is the entry point for the application.

 * <p> The application provides an interface for interacting with an OpenAI GPT model
 * through a command line interface (CLI), a Swing UI or a JavaFX UI.
 * By default, the application runs in CLI mode. Other modes can be specified through command line arguments.
 *
 * <p> The application supports the following command line arguments:
 * '--cli' or '-c' to run in CLI mode (default).
 * '--ui' or '-u' to run in swing UI mode.
 * '--javafx' or '-j' to run in JavaFX UI mode.
 * '--swing' or '-s' to explicitly invoke Swing UI mode, overriding other arguments.
 *
 * <p> For instance, 'java -jar openai-cli.jar --javafx' would launch the application in JavaFX UI mode.
 *
 * @author Jamie Reid
 * @version 0.0.2
 * @since 2023-07-11
 */
public class Main {
    /**
     * The application's entry point.
     *
     * <p> The main method processes command line arguments, sets up the CLI for interacting with the OpenAI GPT model,
     * and then creates and displays the appropriate user interface (CLI, swing UI, or JavaFX UI).
     *
     * <p> A RuntimeException will be caught and printed to the console in case of a failure.
     *
     * @param args an array of command-line arguments for the application
     */
    public static void main(String[] args) {
        boolean runInUIMode = true;

        for (String arg : args) {
            if (arg.equals("--ui") || arg.equals("-u")) {
                runInUIMode = true;
            } else if (arg.equals("--cli") || arg.equalsIgnoreCase("-c")) {
                runInUIMode = false;
            }
        }

        try {
            if (runInUIMode) {  //Run in JavaFX UI mode
                new UserInterfaceScreen( new APIHandler() );
            } else {
                APIHandler api = new APIHandler();
                api.start();
            }
        } catch (RuntimeException runtimeException) {
            System.out.println( "Argggggh, you killed me because of the following reason: " + runtimeException.getMessage() );
            runtimeException.printStackTrace();
        }
    }
}
