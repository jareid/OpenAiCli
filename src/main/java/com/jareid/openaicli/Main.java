package com.jareid.openaicli;

import com.jareid.openaicli.cli.CommandLineInterface;
import com.jareid.openaicli.ui.MainScreen;

import javax.swing.*;

/************************************************************************************
 |                         Copyright Jamie Reid 2023                                |
 ************************************************************************************/
public class Main {
    public static void main(String[] args) {
        try {
            CommandLineInterface cli = new CommandLineInterface();
            cli.run();

            // Run the UI creation on the Event Dispatch Thread (EDT) for thread safety
            //SwingUtilities.invokeLater( () -> new MainScreen( cli) );
        } catch (RuntimeException runtimeException) {
            System.out.println( "Argggggh, you killed me because of the following reason: " + runtimeException.getMessage() );
            runtimeException.printStackTrace();
        }
    }

}
