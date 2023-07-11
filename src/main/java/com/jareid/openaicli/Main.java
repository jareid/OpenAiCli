package com.jareid.openaicli;
/************************************************************************************
 |                         Copyright Jamie Reid 2023                                |
 ************************************************************************************/
public class Main {
    public static void main(String[] args) {
        try {
            CommandLineInterface converter = new CommandLineInterface();
            converter.run();
        } catch (RuntimeException runtimeException) {
            System.out.println( "Argggggh, you killed me because of the following reason: " + runtimeException.getMessage() );
            runtimeException.printStackTrace();
        }
    }

}
