import frontend.*;
import frontend.Parser.Parser;
import frontend.Parser.ParserTreeNode;
import middleend.LlvmIr.IRBuilder;
import middleend.LlvmIr.IRModule;
import frontend.SymbolParser.SymbolTableParser;
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
    static String llvmIrOutputFileName = "llvm_ir.txt";

    private static final Logger logger = Logger.getLogger(Compiler.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
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

        // 语法分析符号表(Deprecated)
        // 仅仅用于语法分析检验错误
        SymbolTableParser symbolTableParser = new SymbolTableParser();

        // 语法分析
        Parser parser = new Parser(tokens,errors, symbolTableParser);
        parser.parse();

        if (!errors.isEmpty()) {
            errors.sort();
            for (int[] error : errors.getErrors()) {
                errorOutput.add(error[0] + " " + (char) error[1]);
            }
            deleteOtherFile(errorOutputFileName);
            writeFile(errorOutputFileName, errorOutput);
        } else {
            // 生成中间代码
            ParserTreeNode root = parser.getRoot();
            IRBuilder irBuilder = new IRBuilder(root);
            IRModule irModule = irBuilder.generateIRModule();
            ArrayList<String> output = irModule.printIR();
            String ans = "";
            int tableCnt = 0;
            for (String s : output) {
                if (s.contains("}")) {
                    tableCnt -= 1;
                }
                if (tableCnt > 0 && !s.startsWith("Label")) {
                    String temp = "";
                    for (int j = 0; j < tableCnt; j++) {
                        temp = temp + "    ";
                    }
                    ans = ans + temp + s;
                } else {
                    ans = ans + s;
                }
                if (s.contains("{")) {
                    tableCnt += 1;
                }
            }
            OutputStream outputStream = new FileOutputStream(llvmIrOutputFileName);
            try {
                outputStream.write(ans.getBytes());
            } catch (IOException e) {
                System.err.println("Can not write " + llvmIrOutputFileName);
            }
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
