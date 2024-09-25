import frontend.Lexer;
import frontend.Token;
import java.io.*;
import java.util.*;

public class Compiler {
    public static void main(String[] args) {
        String inputFileName = "testfile.txt";
        String lexerOutputFileName = "lexer.txt";
        String errorOutputFileName = "error.txt";

        List<String> lines = readFile(inputFileName);
        List<String> lexerOutput = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();

        Lexer lexer = new Lexer();
        boolean hasError = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<Token> tokens = lexer.tokenize(line);
            if (lexer.hasError()) {
                errorOutput.add((i + 1) + " " + lexer.getErrorType());
                hasError = true;
            } else {
                for (Token token : tokens) {
                    lexerOutput.add(token.type() + " " + token.value());
                }
            }
        }

        if (hasError){
            writeFile(errorOutputFileName, errorOutput);
        } else {
            writeFile(lexerOutputFileName, lexerOutput);
        }
    }

    private static List<String> readFile(String fileName) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private static void writeFile(String fileName, List<String> lines) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int i = 0; i < lines.size(); i++) {
                bw.write(lines.get(i));
                if (i < lines.size() - 1) {
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
