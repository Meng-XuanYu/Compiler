import frontend.*;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Compiler {
    private static final Logger logger = Logger.getLogger(Compiler.class.getName());

    public static void main(String[] args) {
        // 文件名称声明
        String inputFileName = "testfile.txt";
        //String lexerOutputFileName = "lexer.txt";
        String errorOutputFileName = "error.txt";
        String parserOutputFileName = "parser.txt";
        String symbolOutputFileName = "symbol.txt";

        // 读取文件
        List<String> lines = readFile(inputFileName);

        // 输出
        //List<String> lexerOutput = new ArrayList<>();
        List<String> errorOutput = new ArrayList<>();
        List<String> parserOutput = new ArrayList<>();
        List<String> symbolOutput = new ArrayList<>();

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
            } //else {
                //for (Token token : tokensTemp) {
                    //lexerOutput.add(token.type() + " " + token.value());
                //}
            //}
        }

        // 符号表
        SymbolTable symbolTable = new SymbolTable();

        // 语法分析
        Parser parser = new Parser(tokens,parserOutput,errors,symbolTable);
        parser.parse();

        if (!errors.isEmpty()) {
            errors.sort();
            for (int[] error : errors.getErrors()) {
                errorOutput.add(error[0] + " " + (char) error[1]);
            }
            writeFile(errorOutputFileName, errorOutput);
        } else {
            //writeFile(lexerOutputFileName, lexerOutput);
            //writeFile(parserOutputFileName, parserOutput);
            symbolOutput = symbolTable.printSymbolTable();
            writeFile(symbolOutputFileName, symbolOutput);
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
}
