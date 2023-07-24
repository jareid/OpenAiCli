package com.jareid.openaicli;

import com.jareid.openaicli.api.APIHandler;
import com.jareid.openaicli.ui.UserInterfaceScreen;
import com.jareid.openaicli.ui.UserInterfaceScreen_Swing;
import javafx.application.Application;
import javafx.stage.Stage;

import javax.swing.*;

/**
 * The {@code MainJavaFX} class is the entry point for the application.
 *
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
public class Main extends Application {
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
        boolean runInUIMode = false;
        boolean runInJavaFXMode = true;

        for (String arg : args) {
            if (arg.equals("--ui") || arg.equals("-u")) {
                runInUIMode = true;
            } else if (arg.equals("--cli") || arg.equalsIgnoreCase("-c")) {
                runInUIMode = false;
            } else if (arg.equals("--javafx") || arg.equalsIgnoreCase("-j")) {
                runInJavaFXMode = true;
            } else if (arg.equals("--swing") || arg.equalsIgnoreCase("-s")) {
                runInJavaFXMode = false;
            }
        }

        try {
            if (runInJavaFXMode) {  //Run in JavaFX UI mode
                launch(args);
            } else {
                APIHandler api = new APIHandler();
                if (runInUIMode) { //Run in swing UI mode
                    // Run the UI creation on the Event Dispatch Thread (EDT) for thread safety
                    SwingUtilities.invokeLater(() -> new UserInterfaceScreen_Swing( api ));
                } else { // Run in Command Line mode
                    api.start();
                }
            }
        } catch (RuntimeException runtimeException) {
            System.out.println( "Argggggh, you killed me because of the following reason: " + runtimeException.getMessage() );
            runtimeException.printStackTrace();
        }
    }

    /**
     * The start method is called after the JavaFX application has been initialized.
     *
     * <p> It sets up the CLI for interacting with the OpenAI GPT model, then creates a new JavaFX user interface for the CLI and displays it.
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set. The primary stage will be embedded in
     * the browser if the application was launched as an applet. Applications
     * may create other stages, if needed, but they will not be primary stages.
     * @throws Exception if an error occurs while loading the FXML or setting up the UI.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        APIHandler cli = new APIHandler();
        UserInterfaceScreen ui = new UserInterfaceScreen(cli);
        ui.setPrimaryStage(primaryStage);
        ui.show();
    }
}
