import frontend.*;
import frontend.Parser.Parser;
import middleend.Symbol.SymbolTable;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Compiler {
    // 文件名称声明
    static String inputFileName = "testfile.txt";
    static String lexerOutputFileName = "lexer.txt";
    static String errorOutputFileName = "error.txt";
    static String parserOutputFileName = "parser.txt";
    static String symbolOutputFileName = "symbol.txt";

    private static final Logger logger = Logger.getLogger(Compiler.class.getName());

    public static void main(String[] args) {
        // 读取文件
        List<String> lines = readFile(inputFileName);

        // 输出
        List<String> errorOutput = new ArrayList<>();

        // 错误列表
        ErrorList errors = new ErrorList();

        // 词法分析
        Lexer lexer = new Lexer();
        List<Token> tokens = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            List<Token> tokensTemp = lexer.tokenize(line, i + 1);
            tokens.addAll(tokensTemp);
            if (lexer.hasError()) {
                errors.addError(i + 1, lexer.getErrorType());
            }
        }

        // 符号表
        SymbolTable symbolTable = new SymbolTable();

        // 语法分析
        Parser parser = new Parser(tokens,errors,symbolTable);
        parser.parse();

        if (!errors.isEmpty()) {
            errors.sort();
            for (int[] error : errors.getErrors()) {
                errorOutput.add(error[0] + " " + (char) error[1]);
            }
            deleteOtherFile(errorOutputFileName);
            writeFile(errorOutputFileName, errorOutput);
        } else {

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
            logger.log(Level.SEVERE, "Error reading file: " + fileName, e);
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
            logger.log(Level.SEVERE, "Error writing file: " + fileName, e);
        }
    }

    private static void deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private static void deleteOtherFile(String fileName) {
        if (fileName.equals(lexerOutputFileName)) {
            deleteFile(parserOutputFileName);
            deleteFile(symbolOutputFileName);
            deleteFile(errorOutputFileName);
        } else if (fileName.equals(parserOutputFileName)) {
            deleteFile(lexerOutputFileName);
            deleteFile(symbolOutputFileName);
            deleteFile(errorOutputFileName);
        } else if (fileName.equals(symbolOutputFileName)) {
            deleteFile(lexerOutputFileName);
            deleteFile(parserOutputFileName);
            deleteFile(errorOutputFileName);
        } else if (fileName.equals(errorOutputFileName)) {
            deleteFile(lexerOutputFileName);
            deleteFile(parserOutputFileName);
            deleteFile(symbolOutputFileName);
        }
    }
}
