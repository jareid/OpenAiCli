package com.jareid.openaicli.cli;

import com.jareid.openaicli.api.APIHandler;

public class CommandLineInterface {
    private APIHandler apiHandler = null;

    public CommandLineInterface( ) {
        apiHandler = new APIHandler( );
    }

    /**
     * A method to start the ChatGPT loop
     * TODO: decide if a thread could be useful, write now in such a simple project it is not useful.
     */
    public void run( ) {
        apiHandler.start( );
    }
}
