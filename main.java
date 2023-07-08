import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.openai.api.models.ChatCompletion;
import com.openai.api.models.Message;

public class ChatGPTConverter {
    private static final String API_KEY = "API KEY HERE";
    private static final String HISTORY_FILE = "history";
    private static final String LAST_PYTHON = "last.py";

    private List<Message> hist;

    public ChatGPTConverter() {
        hist = new ArrayList<>();
    }

    private void readHistoryFromFile() {
        try ( BufferedReader reader = new BufferedReader( new FileReader( HISTORY_FILE ) ) ) {
            String line;
            while ( ( line = reader.readLine( ) ) != null ) {
                hist.add( Message.fromJson( line.trim() ) );
            }
        } catch (IOException readException) {
            System.out.println( "Oooops, couldn't read the history file... Reason: " + readException.getMessage());
            System.out.println( " - - - - - - - Stacktrace start - - - - - - - " );
            readException.printStackTrace();
            System.out.println( " - - - - - - - Stacktrace end - - - - - - - " );
        }
    }

    private void writeHistoryToFile() {
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( HISTORY_FILE ) ) ) {
            for ( Message message : hist ) {
                writer.write( message.toJson( ) );
                writer.newLine( );
            }
        } catch (IOException writeException) {
            System.out.println( "Oooops, couldn't read the history file... Reason: " + writeException.getMessage());
            System.out.println( " - - - - - - - Stacktrace start - - - - - - - " );
            writeException.printStackTrace();
            System.out.println( " - - - - - - - Stacktrace end - - - - - - - " );
        }
    }

    private void writeLastCodeToFile(Message message) {
        try ( BufferedWriter writer = new BufferedWriter( new FileWriter( LAST_CODE_FILE ) ) ) {
            String content = message.getContent();
            List<String> codeList = extractCode(CODE_TYPE.PYTHON.toString(), content);
            for (String code : codeList) {
                writer.write(code);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> extractCode(String type, String content) {
        List<String> codeList = new ArrayList<>();
        String regex = "```python(.*?)```";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String code = matcher.group(1)
                    .replace("\\n", "\n") 
                    .replace("\\\"", "\"")
                    .trim();
            codeList.add(code);
        }
        return codeList;
    }

    private boolean askGPT() {
        System.out.print("You: ");
        Scanner scanner = new Scanner(System.in);
        String userInput = scanner.nextLine();
        if (userInput.equalsIgnoreCase("QUIT")) {
            writeHistoryToFile();
            Message lastItem = hist.get(hist.size() - 1);
            writeLastCodeToFile(lastItem);
            return false;
        }

        hist.add(new Message().setRole("user").setContent(userInput));
        ChatCompletion completion = ChatCompletion.create(API_KEY, hist, 0.5, "gpt-4");
        Message message = completion.getChoices().get(0).getMessage();
        hist.add(message);
        String content = message.getContent();
        System.out.println("ChatGPT: " + content);
        return true;
    }

    public void run() {
        while (true) {
            if (!askGPT()) {
                break;
            }
        }
    }

    public static void main(String[] args) {
        ChatGPTConverter converter = new ChatGPTConverter();
        converter.readHistoryFromFile();
        converter.run();
    }
}
