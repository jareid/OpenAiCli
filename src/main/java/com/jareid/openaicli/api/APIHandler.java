package com.jareid.openaicli.api;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import org.apache.commons.lang3.StringUtils;

/**
 * The {@code CommandLineInterface} class represents a command line interface that interacts with an OpenAI GPT model.
 * It utilizes OpenAI's {@link OpenAiService} to generate text completions based on user input,
 * and provides a command line interface for users to interact with the GPT model.
 * The class maintains a history of interactions which is serialized and deserialized from a file, 
 * and also contains utility methods for handling and writing code blocks present in chat history.
 *
 * <p> The conversation history with the GPT model is persisted in a file named {@code history}.
 * Any code block returned by the GPT model is extracted and saved in a separate file.
 *
 * <p> Please note that the class is not thread-safe. If multiple threads interact with a {@code CommandLineInterface} instance, 
 * it must be synchronized externally.
 *
 * <p> This class requires the OpenAI API key to be provided via the {@code API_KEY} field.
 *
 * @author Jamie Reid
 * @see OpenAiService
 * @see ChatCompletionRequest
 * @see ChatMessage
 * @see ChatMessageRole
 * @version Last updated: 2023-07-24, Version 0.0.2
 * @since 2023-07-08
 */
public class APIHandler {
    private static String OPENAI_MODEL = null;
    private static String HISTORY_FILE_NAME = null;
    private static String CODE_FILE_DATA_FORMAT = null;
    private static String OPENAICLI_CMD_HEADER = null;

    /**
     * A value that represents the regular expression in a ChatMessage response
     */
    private static final String CODE_REGULAR_EXPRESSION = "```(\\w+)?([\\s\\S]*)```";

    /**
     * A field containing the ChatGPT chat history.
     */
    private List<ChatMessage> history;

    /**
     * The OpenAI API Service
     */
    private final OpenAiService service;

    /**
     * A field to control the options of the ChatGPT controller.
     */
    private Map<String, Boolean> options;

    /**
     * The default constructor that initializes the OpenAiService and chat history.
     */
    public APIHandler( ) throws RuntimeException {
        try {
            // Load secret properties
            Properties properties = new Properties();
            properties.load( getClass().getClassLoader( ).getResourceAsStream( "secret.properties" ) );

            String apiKey = properties.getProperty("openai.api.key");
            if ( StringUtils.isEmpty( apiKey ) ) throw new IllegalArgumentException( "OpenAI API key must be set in config.properties" );

            // Load non-secret properties
            properties.load( getClass().getClassLoader( ).getResourceAsStream( "config.properties" ) );

            OPENAI_MODEL = (String) properties.get( "openai.model" );
            if ( StringUtils.isEmpty( OPENAI_MODEL ) ) OPENAI_MODEL = "chatgpt-3.5";

            HISTORY_FILE_NAME = (String) properties.get( "openaicli.filename.history" );
            if ( StringUtils.isEmpty( HISTORY_FILE_NAME ) ) HISTORY_FILE_NAME = "history";

            CODE_FILE_DATA_FORMAT = (String) properties.get( "openaicli.filename.dateFormat" );
            if ( StringUtils.isEmpty( CODE_FILE_DATA_FORMAT ) ) CODE_FILE_DATA_FORMAT = "yyyy-MM-ddHH:mm:ss";

            OPENAICLI_CMD_HEADER = (String) properties.get( "openaicli.commandline.header" );
            if ( StringUtils.isEmpty( OPENAICLI_CMD_HEADER ) ) OPENAICLI_CMD_HEADER = "Open AI CLI --->";

            service = new OpenAiService(apiKey);

            history = new ArrayList<>();

            options = new HashMap<>();

            Boolean disableOutputCodeToFile = (Boolean) properties.get( "openaicli.options.disableOutputCodeToFile" );
            if ( disableOutputCodeToFile == null ) disableOutputCodeToFile = false;
            options.put( "disableOutputCodeToFile", disableOutputCodeToFile );

            Boolean disableLoggingChatGPTHistory = (Boolean) properties.get( "openaicli.options.disableLoggingChatGPTHistory" );
            if ( disableLoggingChatGPTHistory == null ) disableLoggingChatGPTHistory = false;
            options.put( "disableLoggingChatGPTHistory", disableLoggingChatGPTHistory );

            Boolean disableSendingChatGPTHistory = (Boolean) properties.get( "openaicli.options.disableSendingChatGPTHistory" );
            if ( disableSendingChatGPTHistory == null ) disableSendingChatGPTHistory = false;
            options.put( "disableSendingChatGPTHistory", disableSendingChatGPTHistory );
        } catch ( Exception startUpException ) {
            handleException(" couldn't start up the CLI", startUpException );
            throw new RuntimeException( "Failed to read from the history file. Exiting");
        }
    }

    /**
     * This method alternates the boolean value of a specified option in the options HashMap.
     *
     * @param optionName The name of the option to be changed. It should be a valid option name and is case-sensitive.
     *
     * @throws NullPointerException if the specified optionName is null.
     * @throws IllegalArgumentException if the specified optionName does not exist in the options HashMap.
     */
    public void changeOption( String optionName ) {
        options.put( optionName, !options.get( optionName ) );
    }

    /**
     * A method to handle exceptions and print stack trace.
     *
     * @param failMessage the failure message
     * @param exception the thrown exception
     */
    private static void handleException(String failMessage, Exception exception) {
        System.out.println(OPENAICLI_CMD_HEADER + "Oooops, " + failMessage + "... Reason: " + exception.getMessage());
        System.out.println(OPENAICLI_CMD_HEADER + " - - - - - - - Stacktrace start - - - - - - - ");
        exception.printStackTrace();
        System.out.println(OPENAICLI_CMD_HEADER + " - - - - - - - Stacktrace end - - - - - - - ");
    }

    /**
     * A method to read chat history from a file.
     */
    private void readHistoryFromFile() throws RuntimeException {
        File historyFile = createNewHistoryFile();
        if ( historyFile.length() != 0 ) {
            try ( ObjectInputStream inputStream = new ObjectInputStream( new FileInputStream( historyFile ) ) ) {
                history.add((ChatMessage) inputStream.readObject());
            } catch ( ClassNotFoundException | IOException readException ) {
                handleException("couldn't read the history file", readException);
                throw new RuntimeException("Failed to read from the history file. Exiting");
            }
        }
    }

    /**
     * A method to write chat history to a file.
     */
    private void writeHistoryToFile() {
        try ( ObjectOutputStream outputStream = new ObjectOutputStream( new FileOutputStream( HISTORY_FILE_NAME ) ) ) {
            history.forEach( message -> {
                try {
                    outputStream.writeObject( message );
                } catch ( IOException e ) {
                    throw new RuntimeException( e );
                }
            });
        } catch ( IOException writeException ) {
            handleException( "couldn't write the history file", writeException );
            throw new RuntimeException( "Failed to write the the code file. Exiting");
        }
    }

    /**
     * A method to write chat history to a file.
     * TODO: rename old history with date.
     * */
    private void clearHistoryToFile() {
        renameHistoryFile();
        createNewHistoryFile();
        writeHistoryToFile();
    }

    /**
     * Renames the history file to a new file with the date as the file type.
     */
    private static void renameHistoryFile() {
        File originalFile = new File( HISTORY_FILE_NAME ); // Create a File object with the old file path
        File renamedFile = new File(originalFile.getParent(), HISTORY_FILE_NAME + "." + generateDateString() ); // Create a File object with the new file path

        // Rename the file and check for success
        if ( originalFile.renameTo( renamedFile ) ) {
            System.out.println(OPENAICLI_CMD_HEADER + " History file renamed successfully.");
        } else {
            System.out.println(OPENAICLI_CMD_HEADER + " History file renaming failed.");
        }
    }

    /**
     * Creates a new history file (without extension).
     * Used for a variety of reasons.
     */
    private static File createNewHistoryFile( ) {
        File historyFile = new File( HISTORY_FILE_NAME ); // Check if the file already exists
        try {
            if ( !historyFile.exists() ) {
                // Create the file and check for success
                if ( historyFile.createNewFile( ) ) {
                    System.out.println(OPENAICLI_CMD_HEADER + " History file created successfully.");
                } else {
                    System.out.println(OPENAICLI_CMD_HEADER + " History file creation failed.");
                }
            } else {
                System.out.println(OPENAICLI_CMD_HEADER + " History file already exists.");
            }
            return historyFile;
        } catch ( IOException createException ) {
            handleException( "couldn't create the history file", createException );
            throw new RuntimeException( "Failed to create the history file. Exiting");
        }
    }

    /**
     * A method to generate data string
     *
     * @return a generate date string
     */
    private static String generateDateString( ) {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( CODE_FILE_DATA_FORMAT );
        return currentDateTime.format(formatter);
    }

    /**
     * A method to generate file name for code files.
     *
     * @param codeType the type of code
     * @return a formatted file name
     */
    private static String generateCodeFileName( String codeType ) {
        return generateDateString( ) + ( StringUtils.isEmpty(codeType) ? "" : "." + codeType );
    }

    private static boolean hasCode( ChatMessage message ) {
        String content = message.getContent( );
        Pattern pattern = Pattern.compile(CODE_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher( content );
        return matcher.find();
    }

    /**
     * A method to check if a ChatMessage contains code.
     *
     * @param message the chat message
     * @return codeType if it contains code, "" otherwise
     */
    private static String extractCodeType( ChatMessage message ) {
        String content = message.getContent( );
        Pattern pattern = Pattern.compile(CODE_REGULAR_EXPRESSION);
        Matcher matcher = pattern.matcher( content );
        if (matcher.find()) return matcher.group(1);
        else return "";
    }

    /**
     * A method to extract the code type from a ChatMessage.
     *
     * @param message the chat message
     * @return the extracted code type
     */
    private List<String> extractCode( ChatMessage message ) {
        String content = message.getContent( );
        List<String> codeList = new ArrayList<>();

        Pattern pattern = Pattern.compile(CODE_REGULAR_EXPRESSION, java.util.regex.Pattern.DOTALL);
        Matcher matcher = pattern.matcher( content );

        while (matcher.find()) {
            String codeBlock = matcher.group( 1 ).replace( "\\n", "\n" )
                                                 .replace( "\\\"", "\"" )
                                                 .trim( );
            codeList.add( codeBlock );
        }
        return codeList;
    }

    /**
     * A method to write the code from a ChatMessage to a file.
     *
     * @param message the chat message
     */
    private void writeCodeToFile( ChatMessage message ) throws RuntimeException {
        if (options.get("disableOutputCodeToFile")) return;

        String codeType = extractCodeType( message );
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( generateCodeFileName( codeType ) ) ) ) {
            List<String> codeList = extractCode( message );
            codeList.forEach(code -> {
                try {
                    writer.write(code);
                    writer.newLine();
                } catch (IOException writeCodeLineException) {
                    handleException( "couldn't next line to the code file", writeCodeLineException );
                }
            });
        } catch ( IOException writeCodeException ) {
            handleException( "couldn't write to the code file", writeCodeException );
            throw new RuntimeException( "Failed to write the the code file. Exiting");
        }
    }

    /**
     * A method to handle user inputs and interact with OpenAI.
     *
     * @return true if the chat should continue, false otherwise
     */
    private boolean askGPT() {
        System.out.print("You: ");
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        return askGPT( userInput );
    }


    /**
     * A method to handle user inputs and interact with OpenAI.
     * TODO: modify for UI usage.
     *
     * @param userInput THe input from the command line or from the UI
     *
     * @return true if the chat should continue, false otherwise
     */
    public boolean askGPT( String userInput ) {
        if ( userInput.equalsIgnoreCase( "QUIT" ) ||
             userInput.equalsIgnoreCase( "WRITELAST" ) ) {
            writeHistoryToFile();

            // If we received QUIT return false to exit the program
            if( userInput.equalsIgnoreCase( "QUIT" ) ) return false;
        } else if ( userInput.equalsIgnoreCase( "WIPE" ) ||
                    userInput.equalsIgnoreCase( "WIPEHISTORY" ) ) {
            history = new ArrayList<>( );
            clearHistoryToFile( );
        }

        ChatMessage response = askGPT_GetResponse( userInput );

        if ( hasCode( response ) ) writeCodeToFile( response );

        System.out.print( "ChatGPT: " + response.getContent( ) + System.lineSeparator( ) );

        return false;
    }

    public ChatMessage askGPT_GetResponse( String userInput ) {
        ChatMessage userMessage = new ChatMessage( ChatMessageRole.USER.value(), userInput );
        if ( !options.get( "disableLoggingChatGPTHistory" ) ) history.add( userMessage );

        // Process the user's message with OpenAI
        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder( )
                                                                 .model( OPENAI_MODEL ) // see https://platform.openai.com/docs/models
                                                                  // if option enabled, send history
                                                                 .messages( !options.get( "disableSendingChatGPTHistory" ) ? history : null )
                                                                 .maxTokens( 256 )
                                                                 .build( );

        ChatMessage response = service.createChatCompletion( chatRequest ).getChoices( )
                                                                          .get( 0 )
                                                                          .getMessage( );

        if ( !options.get( "disableLoggingChatGPTHistory" ) ) history.add( response );  // Add the last user message to history

        return response;
    }

    /**
     * A method to start the chat loop.
     * TODO: decide if a thread could be useful, write now in such a simple project it is not useful.
     */
    public void start() {
        // Creating a new thread
        //Thread thread = new Thread(() -> {
        while (true) {
            if (!askGPT()) {
                break;
            }
        }
        //});

        // Starting the thread
        //thread.start();
    }
}
