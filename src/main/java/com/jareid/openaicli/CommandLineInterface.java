package com.jareid.openaicli;
/************************************************************************************
 |                         Copyright Jamie Reid 2023                                |
 ************************************************************************************/
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
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
 * @version 1.0
 * @since 2023-07-08
 */
public class CommandLineInterface {
    private static String API_KEY = null;
    private static String OPENAI_MODEL = null;
    private static String HISTORY_FILE_NAME = null;
    private static String CODE_FILE_DATA_FORMAT = null;
    private static String OPENAICLI_CMD_HEADER = null;

    /**
     * A value that represents the regular expression in a ChatMessage response
     * TODO: change to handle more than one code block
     */
    private static String CODE_REGULAR_EXPORESSION = "```(\\w+)?([\\s\\S]*)```";

    private List<ChatMessage> history;

    private OpenAiService service;

    /**
     * The default constructor that initializes the OpenAiService and chat history.
     */
    public CommandLineInterface() throws RuntimeException {
        try {
            // Load properties
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));

            API_KEY = properties.getProperty("openai.api.key");
            if ( StringUtils.isEmpty( API_KEY ) ) throw new IllegalArgumentException("OpenAI API key must be set in config.properties");

            OPENAI_MODEL = (String) properties.get("openai.model");
            if ( StringUtils.isEmpty( OPENAI_MODEL ) ) OPENAI_MODEL = "chatgpt-3.5";

            HISTORY_FILE_NAME = (String) properties.get("openaicli.filename.history");
            if ( StringUtils.isEmpty( HISTORY_FILE_NAME ) ) HISTORY_FILE_NAME = "history";

            CODE_FILE_DATA_FORMAT = (String) properties.get("openaicli.filename.dateFormat");
            if ( StringUtils.isEmpty( API_KEY ) ) CODE_FILE_DATA_FORMAT = "yyyy-MM-ddHH:mm:ss";

            OPENAICLI_CMD_HEADER = (String) properties.get("openaicli.commandline.header");
            if ( StringUtils.isEmpty( OPENAICLI_CMD_HEADER ) ) OPENAICLI_CMD_HEADER = "Open AI CLI --->";



            service = new OpenAiService(API_KEY);
            history = new ArrayList<>();

        } catch ( Exception startUpException ) {
            handleException(" couldn't start up the CLI", startUpException );
            throw new RuntimeException( "Failed to read from the history file. Exiting");
        }
    }

    /**
     * A method to handle exceptions and print stack trace.
     *
     * @param failMessage the failure message
     * @param exception the thrown exception
     */
    private static void handleException(String failMessage, Exception exception) {
        System.out.println("Oooops, " + failMessage + "... Reason: " + exception.getMessage());
        System.out.println(" - - - - - - - Stacktrace start - - - - - - - ");
        exception.printStackTrace();
        System.out.println(" - - - - - - - Stacktrace end - - - - - - - ");
    }

    /**
     * A method to read chat history from a file.
     */
    private void readHistoryFromFile() throws RuntimeException {
        File historyFile = new File( HISTORY_FILE_NAME );
        try {
            if ( !historyFile.exists() ) {
                historyFile.createNewFile();
            }
        } catch ( IOException createException ) {
            handleException( "couldn't create the history file", createException );
            throw new RuntimeException( "Failed to create the history file. Exiting");
        }
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
        File oldFile = new File( HISTORY_FILE_NAME ); // Create a File object with the old file path
        File newFile = new File(oldFile.getParent(), HISTORY_FILE_NAME + "." + generateDateString() ); // Create a File object with the new file path
        boolean isRenamed = oldFile.renameTo(newFile); // Rename the file

        // Check if the file was successfully renamed
        if (isRenamed) {
            System.out.println("File renamed successfully.");
        } else {
            System.out.println("File renaming failed.");
        }

        try {
            File historyFile = new File( HISTORY_FILE_NAME );
            if ( !historyFile.exists() ) {
                boolean isCreated = historyFile.createNewFile();
                if (isRenamed) {
                    System.out.println("File renamed successfully.");
                } else {
                    System.out.println("File renaming failed.");
                }
            }
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
        Pattern pattern = Pattern.compile( CODE_REGULAR_EXPORESSION );
        Matcher matcher = pattern.matcher( content );
        return matcher.find();
    }

    /**
     * A method to check if a ChatMessage contains code.
     *
     * @param message the chat message
     * @return true if it contains code, false otherwise
     */
    private static String extractCodeType( ChatMessage message ) {
        String content = message.getContent( );
        Pattern pattern = Pattern.compile( CODE_REGULAR_EXPORESSION );
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

        Pattern pattern = Pattern.compile( CODE_REGULAR_EXPORESSION , java.util.regex.Pattern.DOTALL);
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
        if (userInput.equalsIgnoreCase("QUIT")) {
            writeHistoryToFile();
            ChatMessage lastMessage = history.get(history.size() - 1);
            if ( hasCode( lastMessage ) ) {
                writeCodeToFile( lastMessage );
            }
            return false;
        } else if ( userInput.equalsIgnoreCase("WIPE") ||
                    userInput.equalsIgnoreCase("WIPEHISTORY") ) {
            history = new ArrayList<>( );
            clearHistoryToFile( );
        }

        // Add the last user message to history
        ChatMessage userMessage = new ChatMessage( ChatMessageRole.USER.value(), userInput );
        history.add( userMessage );

        // Process the user's message with OpenAI
        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder( )
                                                                 .model( OPENAI_MODEL ) // see https://platform.openai.com/docs/models
                                                                 .messages( history )
                                                                 .maxTokens( 256 )
                                                                 .build( );

        ChatMessage response = service.createChatCompletion( chatRequest ).getChoices( )
                                                                          .get( 0 )
                                                                          .getMessage( );

        history.add( response ); // Add the last OpenAI message to history
        System.out.print( "ChatGPT: " + response.getContent( )  + System.lineSeparator( ) );
        return true;
    }

    /**
     * A method to start the chat loop.
     * TODO: decide if a thread could be useful, write now in such a simple project it is not useful.
     */
    protected void run() {
        readHistoryFromFile();
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
