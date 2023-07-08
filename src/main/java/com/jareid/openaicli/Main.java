package com.jareid.openaicli;
/************************************************************************************
 |                         Copyright Jamie Reid 2023                                |
 ************************************************************************************/
public class Main {
    public static void main(String[] args) {
        CommandLineInterface converter = new CommandLineInterface();
        converter.readHistoryFromFile();
        converter.run();
    }

}
