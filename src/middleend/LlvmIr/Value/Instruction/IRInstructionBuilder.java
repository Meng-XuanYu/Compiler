package middleend.LlvmIr.Value.Instruction;

import frontend.Parser.ParserTreeNode;
import frontend.SymbolParser.SymbolType;
import frontend.SyntaxType;
import frontend.TokenType;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.Function.FunctionCnt;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRAlloca;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRLoad;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRStore;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRCall;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRGoto;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRRet;
import middleend.Symbol.*;

import java.util.ArrayList;

import static middleend.LlvmIr.Value.GlobalVar.IRGlobalVarBuilder.setConstInit;

// 除了for,if,Block的Stmt
public class IRInstructionBuilder {
    // 当前信息
    private SymbolTable symbolTable; // 当前符号表
    private IRBasicBlock block; // 当前基本块
    private ParserTreeNode blockItem; // 当前基本块元素
    private ArrayList<IRInstruction> instructions; // 当前基本块指令
    private FunctionCnt functionCnt; // 当前计数器

    private IRValue left; // 左值
    private ParserTreeNode addExp;

    // 看传入的是哪种BlockItem
    private ParserTreeNode stmtAssign; // 赋值语句
    private ParserTreeNode stmtBreak; // 处理break
    private ParserTreeNode stmtContinue; // 处理continue
    private ParserTreeNode stmtReturn; // 处理return
    private ParserTreeNode stmtFor; // 处理for
    private ParserTreeNode stmtIf; // 处理if
    private ParserTreeNode stmtInput; // 处理输入
    private ParserTreeNode stmtOutput; // 处理输出
    private ParserTreeNode stmtExp; // 处理表达式
    private ParserTreeNode stmt;

    // 处理continue,break
    private IRLabel forBegin;
    private IRLabel forEnd;

    public IRInstructionBuilder() {

    }


    public IRInstructionBuilder(SymbolTable symbolTable,
                                IRBasicBlock block,
                                FunctionCnt functionCnt,
                                IRLabel forBegin,
                                IRLabel forEnd) {
        this.symbolTable = symbolTable;
        this.block = block;
        this.instructions = new ArrayList<>();
        this.functionCnt = functionCnt;
        this.forBegin = forBegin;
        this.forEnd = forEnd;
    }

    // 解析元素是blockItem
    public IRInstructionBuilder(ParserTreeNode blockItem,
                                SymbolTable symbolTable,
                                IRBasicBlock block,
                                FunctionCnt functionCnt,
                                IRLabel forBegin,
                                IRLabel forEnd) {
        this(symbolTable, block, functionCnt, forBegin, forEnd);
        this.blockItem = blockItem;
    }

    public IRInstructionBuilder(SymbolTable symbolTable,
                                IRBasicBlock basicBlock,
                                ParserTreeNode addExp,
                                FunctionCnt functionCnt,
                                IRLabel whileLabel,
                                IRLabel endLabel) {
        this(symbolTable, basicBlock, functionCnt, whileLabel, endLabel);
        this.addExp = addExp;
    }

    public IRInstructionBuilder(SymbolTable symbolTable,
                                ParserTreeNode stmt,
                                IRBasicBlock basicBlock,
                                FunctionCnt functionCnt,
                                IRLabel whileLabel,
                                IRLabel endLabel) {
        this(symbolTable, basicBlock, functionCnt, whileLabel, endLabel);
        this.stmt = stmt;
    }

    public IRValue getLeft() {
        return left;
    }

    public IRValue generateZero() {
        return new IRValue("0", IRIntegerType.get32());
    }

    public ArrayList<IRInstruction> generateInstructions() {
        if (blockItem != null) {
            if (this.blockItem.getType() == SyntaxType.Decl) {
                ParserTreeNode decl = blockItem.getFirstChild();
                if (decl.getType() == SyntaxType.ConstDecl) {
                    ParserTreeNode constDecl = decl.getFirstChild();
                    generateIRInstructionConstDecl(constDecl);
                } else {
                    // VarDecl
                    ParserTreeNode varDecl = decl.getFirstChild();
                    generateIRInstructionVarDecl(varDecl);
                }
            } else {
                // Stmt
                ParserTreeNode stmt = blockItem.getFirstChild();
                if (stmt.getFirstChild().getType() == SyntaxType.Token) {
                    TokenType tokenType = stmt.getFirstChild().getToken().type();
                    if (tokenType == TokenType.IFTK) {
                        // 不可能
                    } else if (tokenType == TokenType.FORTK) {
                        // 不可能
                    } else if (tokenType == TokenType.BREAKTK) {
                        this.stmtBreak = stmt;
                        generateIRinstructionFromStmtBreak();
                    } else if (tokenType == TokenType.CONTINUETK) {
                        this.stmtContinue = stmt;
                        generateIRinstructionFromStmtContinue();
                    } else if (tokenType == TokenType.RETURNTK) {
                        this.stmtReturn = stmt;
                        generateIRinstructionFromStmtReturn();
                    } else {
                        // tokenType == TokenType.SEMICN
                    }
                } else if (stmt.getFirstChild().getType() == SyntaxType.LVal) {
                    if (stmt.getChildren().get(2).getType() == SyntaxType.Token) {
                        TokenType tokenType = stmt.getChildren().get(2).getToken().type();
                        if (tokenType == TokenType.GETINTTK) {
                            this.stmtInput = stmt;
                            generateIRinstructionFromStmtInputInt();
                        } else {
                            this.stmtInput = stmt;
                            generateIRinstructionFromStmtInputChar();
                        }
                    } else {
                        this.stmtAssign = stmt;
                        generateIRinstructionFromStmtAssign();
                    }
                } else if (stmt.getFirstChild().getType() == SyntaxType.Block) {
                    // 不可能
                } else if (stmt.getFirstChild().getType() == SyntaxType.Exp) {
                    this.stmtExp = stmt;
                    generateIRInstructionFromStmtExp();
                } else {
                    this.stmtOutput = stmt;
                    generateIRinstructionFromStmtOutput();
                }
            }
        } else if (this.addExp != null) {
            generateIRInstructionFromAddExp(this.addExp, false);
        } else {
            // Stmt
            ParserTreeNode stmt = this.stmt;
            if (stmt.getFirstChild().getType() == SyntaxType.Token) {
                TokenType tokenType = stmt.getFirstChild().getToken().type();
                if (tokenType == TokenType.IFTK) {
                    // 不可能
                } else if (tokenType == TokenType.FORTK) {
                    // 不可能
                } else if (tokenType == TokenType.BREAKTK) {
                    this.stmtBreak = stmt;
                    generateIRinstructionFromStmtBreak();
                } else if (tokenType == TokenType.CONTINUETK) {
                    this.stmtContinue = stmt;
                    generateIRinstructionFromStmtContinue();
                } else if (tokenType == TokenType.RETURNTK) {
                    this.stmtReturn = stmt;
                    generateIRinstructionFromStmtReturn();
                } else {
                    // tokenType == TokenType.SEMICN
                }
            } else if (stmt.getFirstChild().getType() == SyntaxType.LVal) {
                if (stmt.getChildren().get(2).getType() == SyntaxType.Token) {
                    TokenType tokenType = stmt.getChildren().get(2).getToken().type();
                    if (tokenType == TokenType.GETINTTK) {
                        this.stmtInput = stmt;
                        generateIRinstructionFromStmtInputInt();
                    } else {
                        this.stmtInput = stmt;
                        generateIRinstructionFromStmtInputChar();
                    }
                } else {
                    this.stmtAssign = stmt;
                    generateIRinstructionFromStmtAssign();
                }
            } else if (stmt.getFirstChild().getType() == SyntaxType.Block) {
                // 不可能
            } else if (stmt.getFirstChild().getType() == SyntaxType.Exp) {
                this.stmtExp = stmt;
                generateIRInstructionFromStmtExp();
            } else {
                this.stmtOutput = stmt;
                generateIRinstructionFromStmtOutput();
            }
        }
        return this.instructions;
    }

    private void generateIRinstructionFromStmtOutput() {
        ParserTreeNode formatString = stmtOutput.getChildren().get(2);
        ArrayList<ParserTreeNode> exps = stmtOutput.getOutputExps();
        String format = formatString.getToken().value();
        char[] chars = format.substring(1,format.length()-1).toCharArray();
        int cnt = 0;

        ArrayList<IRValue> values = new ArrayList<>();
        for (ParserTreeNode exp : exps) {
            values.add(generateIRInstructionFromExp(exp, false));
        }
        for (int i = 0; i < chars.length;i++) {
            char c = chars[i];
            IRCall call;
            if (c == '%' && i + 1 < chars.length && chars[i + 1] == 'd') {
                IRValue value = values.get(cnt);
                call = new IRCall("@putint", value);
                i++;
                cnt++;
            } else if (c == '\\' && i + 1 < chars.length && chars[i + 1] == 'n') {
                call = new IRCall("@putch", '\n');
            } else if (c == '%' && i + 1 < chars.length && chars[i + 1] == 'c') {
                IRValue value = values.get(cnt);
                call = new IRCall("@putch", value);
                i++;
                cnt++;
            } else if (c == '%' && i + 1 < chars.length && chars[i + 1] == 's') {
                IRValue value = values.get(cnt);
                call = new IRCall("@putstr", value);
                i++;
                cnt++;
            } else {
                call = new IRCall("@putch", c);
            }
            this.instructions.add(call);
        }
    }

    private void generateIRInstructionFromStmtExp() {
        ParserTreeNode exp = stmtExp.getFirstChild();
        generateIRInstructionFromExp(exp, false);
    }

    private void generateIRinstructionFromStmtInputChar() {
        ParserTreeNode lVal = stmtInput.getChildren().get(0);
        IRValue left = generateIRInstructionFromLVal(lVal, true);
        IRCall call = new IRCall("@getchar");
        call.setName("%LocalVar" + functionCnt.getCnt());
        this.instructions.add(call);

        IRStore store;
        if (left.getSize() == 0) {
            store = new IRStore(call, left);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(lVal.getChildren().get(2), false);
            store = new IRStore(call, left, 0, 1, null, dimension1PointerValue);
        }
        this.instructions.add(store);
    }

    private void generateIRinstructionFromStmtInputInt() {
        ParserTreeNode lVal = stmtInput.getChildren().get(0);
        IRValue left = generateIRInstructionFromLVal(lVal, true);
        IRCall call = new IRCall("@getint");
        call.setName("%LocalVar" + functionCnt.getCnt());
        this.instructions.add(call);

        IRStore store;
        if (left.getSize() == 0) {
            store = new IRStore(call, left);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(lVal.getChildren().get(2), false);
            store = new IRStore(call, left, 0, 1, null, dimension1PointerValue);
        }
        this.instructions.add(store);
    }

    private void generateIRinstructionFromStmtReturn() {
        IRRet ret ;
        if (this.stmtReturn.getChildren().size() == 2) {
            ret = new IRRet();
        } else {
            ParserTreeNode exp = this.stmtReturn.getChildren().get(1);
            IRValue value = generateIRInstructionFromExp(exp, false);
            ret = new IRRet(value);
        }
        this.instructions.add(ret);
    }

    private void generateIRinstructionFromStmtContinue() {
        IRGoto gotoBegin = new IRGoto(this.forBegin);
        this.instructions.add(gotoBegin);
    }

    private void generateIRinstructionFromStmtBreak() {
        IRGoto gotoEnd = new IRGoto(this.forEnd);
        this.instructions.add(gotoEnd);
    }

    private void generateIRinstructionFromStmtAssign() {
        ParserTreeNode lVal = stmtAssign.getFirstChild();
        ParserTreeNode exp = stmtAssign.getChildren().get(2);

        IRValue left = generateIRInstructionFromLVal(lVal, true);
        IRValue right = generateIRInstructionFromExp(exp, false);
        int leftSize = left.getSize();
        int rightSize = right.getSize();

        IRStore store;
        if (leftSize == 0) {
            store = new IRStore(right, left);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(exp, false);
            store = new IRStore(right, left, rightSize, leftSize, null, dimension1PointerValue);
        }
        this.instructions.add(store);
    }

    private void generateIRInstructionVarDecl(ParserTreeNode varDecl) {
        for (int i = 2; i < varDecl.getChildren().size(); i += 2) {
            ParserTreeNode varDef = varDecl.getChildren().get(i);
            addVar(varDef, varDecl.getChildren().get(1).getChildren().get(0).getToken().type() == TokenType.CHARTK);
        }
    }

    private void addVar(ParserTreeNode varDef, boolean isChar) {
        IRIntegerType irIntegerType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
        Symbol symbol;
        String name = "%LocalVar" + functionCnt.getCnt();
        if (varDef.varDefHasAssign()) {
            ParserTreeNode varDefInit = varDef.getLastChild();
            if (varDef.hasLbrack()) {
                // 数组
                if (isChar) {
                    symbol = new Symbol(name, SymbolType.CharArray);
                } else {
                    symbol = new Symbol(name, SymbolType.IntArray);
                }
                this.symbolTable.addSymbol(symbol);
                IRValue value = new IRValue(name, irIntegerType);
                value.setSize(varDef.getChildren().get(2).getChildren().size());
                symbol.setValue(value);
                // IRAlloca指令
                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setSize(value.getSize());
                alloca.setName(name);
                this.instructions.add(alloca);

                alloca.setSize(value.getSize());
                alloca.setName(name);
                this.instructions.add(alloca);

                int cnt = 0; // 数组下标
                if (varDefInit.getFirstChild().getType() == SyntaxType.Token && varDefInit.getFirstChild().getToken().type() == TokenType.STRCON) {
                    // 字符串
                    String str = varDefInit.getFirstChild().getToken().value().substring(1, varDefInit.getToken().value().length() - 1);
                    for (int i = 0; i < str.length(); i++) {
                        IRValue initValue = new IRValue(String.valueOf(str.charAt(i)), irIntegerType);
                        this.instructions.add(new IRStore(initValue, value, 0, 1, -1, i));
                    }
                } else {
                    // 表达式{}
                    for (int i = 1; i < varDefInit.getChildren().size(); i+=2) {
                        IRValue right = new IRValue(varDefInit.getChildren().get(i).getToken().value(), irIntegerType);
                        IRValue initValue = new IRValue(varDefInit.getChildren().get(i).getToken().value(), irIntegerType);
                        this.instructions.add(new IRStore(initValue, value, 0, 1, -1, i));
                    }
                }
            } else {
                // 单个变量
                if (isChar) {
                    symbol = new Symbol(name, SymbolType.Char);
                } else {
                    symbol = new Symbol(name, SymbolType.Int);
                }
                this.symbolTable.addSymbol(symbol);
                IRValue value = new IRValue(name, irIntegerType);
                value.setName(name);

                SymbolType symbolType = isChar ? SymbolType.Char : SymbolType.Int;
                SymbolVar symbolVar = new SymbolVar(name, symbolType, value);
                this.symbolTable.addSymbol(symbolVar);

                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setName(name);
                alloca.setSize(0);
                this.instructions.add(alloca);

                ParserTreeNode initVal = varDef.getLastChild();
                if (initVal.getType() == SyntaxType.Exp) {
                    // 表达式
                    IRValue initValue = new IRValue(initVal.getToken().value(), irIntegerType);
                    this.instructions.add(new IRStore(initValue, value, 0, 1, -1, 0));
                } else {
                    // 字符串
                    String str = initVal.getToken().value();
                    for (int i = 0; i < str.length(); i++) {
                        IRValue initValue = new IRValue(String.valueOf(str.charAt(i)), irIntegerType);
                        this.instructions.add(new IRStore(initValue, value, 0, 1, -1, i));
                    }
                }
            }
        } else {

        }
    }

    private IRValue generateIRInstructionFromExp(ParserTreeNode exp, boolean isLeft) {
        return generateIRInstructionFromAddExp(exp.getFirstChild(), isLeft);
    }

    private IRValue generateIRInstructionFromAddExp(ParserTreeNode addExp, boolean isLeft) {
        IRValue ans;
        ParserTreeNode mulExp = addExp.getFirstMulExp();
        IRValue left = generateIRInstructionFromMulExp(mulExp, isLeft);
        if (addExp.getChildren().size() == 1) {
            ans = left;
        } else {
            ArrayList<ParserTreeNode> operantors = addExp.getOtherMulExps();
            ArrayList<TokenType> operators = addExp.getOperators();
            IRValue right;
            int len = operantors.size();
            IRValueType irValueType = IRIntegerType.get32();
            for (int i = 0; i < len ; i++) {
                ParserTreeNode mulExp1 = operantors.get(i);
                TokenType operator = operators.get(i);
                right = generateIRInstructionFromMulExp(mulExp1, false);
                IRBinaryInstruction add;
                if (operator == TokenType.PLUS) {
                    add = new IRBinaryInstruction(irValueType,IRInstructionType.Add, left, right);
                } else {
                    add = new IRBinaryInstruction(irValueType,IRInstructionType.Sub,  left, right);
                }
                String name = "%LocalVar" + functionCnt.getCnt();
                add.setName(name);
                this.instructions.add(add);
                left = add;
            }
            ans = left;
        }
        this.left = left;
        return ans;
    }

    private IRValue generateIRInstructionFromMulExp(ParserTreeNode mulExp, boolean isLeft) {
        IRValue ans;
        ParserTreeNode unaryExp = mulExp.getFirstUnaryExp();
        IRValue left = generateIRInstructionFromUnaryExp(unaryExp, isLeft);
        if (mulExp.getChildren().size() == 1) {
            ans = left;
        } else {
            ArrayList<ParserTreeNode> operantors = mulExp.getOtherUnaryExps();
            ArrayList<TokenType> operators = mulExp.getOperators();
            IRValue right;
            int len = operantors.size();
            IRValueType irValueType = IRIntegerType.get32();
            for (int i = 0; i < len ; i++) {
                ParserTreeNode unaryExp1 = operantors.get(i);
                TokenType operator = operators.get(i);
                right = generateIRInstructionFromUnaryExp(unaryExp1, false);
                IRBinaryInstruction mul;
                if (operator == TokenType.MULT) {
                    mul = new IRBinaryInstruction(irValueType,IRInstructionType.Mul, left, right);
                } else if (operator == TokenType.DIV) {
                    mul = new IRBinaryInstruction(irValueType,IRInstructionType.Div,  left, right);
                } else {
                    mul = new IRBinaryInstruction(irValueType,IRInstructionType.Mod,  left, right);
                }
                String name = "%LocalVar" + functionCnt.getCnt();
                mul.setName(name);
                this.instructions.add(mul);
                left = mul;
            }
            ans = left;
        }
        this.left = left;
        return ans;
    }

    private IRValue generateIRInstructionFromUnaryExp(ParserTreeNode unaryExp, boolean isLeft) {
        IRValue ans = null;
        ParserTreeNode element = unaryExp.getFirstChild();
        if (element.getType() == SyntaxType.PrimaryExp) {
            ans = generateIRInstructionFromPrimaryExp(element, isLeft);
        } else if (element.getType() == SyntaxType.UnaryOp){
            ans = generateIRInstructionFromUnaryExpOp(unaryExp, isLeft);
        } else if (element.getType() == SyntaxType.Token) {
            ans = generateIRInstructionFromFuncCall(unaryExp, isLeft);
        } else {
            System.out.println("Error: generateIRInstructionFromUnaryExp");
        }
        return ans;
    }

    private IRValue generateIRInstructionFromUnaryExpOp(ParserTreeNode unaryExp, boolean isLeft) {
        IRValue ans = null;
        ParserTreeNode unaryExp_child = unaryExp.getChildren().get(1);
        ParserTreeNode unaryOp = unaryExp.getChildren().get(0);
        TokenType type = unaryOp.getToken().type();
        if (type == TokenType.PLUS) {
            ans = generateIRInstructionFromUnaryExp(unaryExp_child, isLeft);
        } else if (type == TokenType.MINU) {
            IRValue left = new IRValue("0", IRIntegerType.get32());
            IRBinaryInstruction mulExp = new IRBinaryInstruction(IRIntegerType.get32(),
                    IRInstructionType.Mul, left, generateIRInstructionFromUnaryExp(unaryExp, isLeft));
            String name = "%LocalVar" + functionCnt.getCnt();
            mulExp.setName(name);
            this.instructions.add(mulExp);
            ans = mulExp;
        } else if (type == TokenType.NOT) {
            IRBinaryInstruction not = new IRBinaryInstruction(IRIntegerType.get32(),
                    IRInstructionType.Not, generateIRInstructionFromUnaryExp(unaryExp_child, isLeft), null);
            String name = "%LocalVar" + functionCnt.getCnt();
            not.setName(name);
            this.instructions.add(not);
            ans = not;
        } else {
            System.out.println("Error: generateIRInstructionFromUnaryExpOp");
        }
        return ans;
    }

    private IRValue generateIRInstructionFromFuncCall(ParserTreeNode funcCall, boolean isLeft) {
        IRCall call = null;
        String name = funcCall.getFirstChild().getToken().value();
        ArrayList<IRValue> args = new ArrayList<>();
        Symbol symbol = this.symbolTable.getSymbol(name);
        IRFunction function = (IRFunction)symbol.getValue();

        ParserTreeNode funcRParams = funcCall.getChildren().get(2);
        if (funcRParams.getType() == SyntaxType.FuncRParams) {
            SymbolFunc symbolFunc = (SymbolFunc)symbol;
            ArrayList<Symbol> symbols = symbolFunc.getSymbols();
            IRValue arg = null;
            int k = 0;
            for (int i = 0; i < funcRParams.getChildren().size(); i += 2) {
                ParserTreeNode exp = funcRParams.getChildren().get(i);
                Symbol symbolNowExp = symbols.get(k);
                if (symbolNowExp.isArray()) {
                    arg = generateIRInstructionFromExp(exp, true);
                    args.add(arg);
                } else {
                    arg = generateIRInstructionFromExp(exp, true);
                    IRValue left = new IRValue(arg.getType());
                    String name_alloca = "%LocalVar" + functionCnt.getCnt();
                    IRAlloca alloca = new IRAlloca(arg.getType(), left);
                    alloca.setName(name_alloca);
                    this.instructions.add(alloca);
                    IRStore store = new IRStore(arg, left);
                    left.setName(name_alloca);
                    store.setName(name_alloca);
                    this.instructions.add(store);
                    args.add(store);
                }
                k++;
            }
            call = new IRCall(function, args);
        } else {
            // 没有参数
            call = new IRCall(function, args);
        }
        if (!call.isVoid()) {
            String ans_name = "%LocalVar" + functionCnt.getCnt();
            call.setName(ans_name);
            this.instructions.add(call);
            return call;
        } else {
            this.instructions.add(call);
            return null;
        }
    }

    private IRValue generateIRInstructionFromPrimaryExp(ParserTreeNode primaryExp, boolean isLeft) {
        IRValue ans = null;
        ParserTreeNode element = primaryExp.getFirstChild();
        if (element.getType() == SyntaxType.Token && element.getToken().type() == TokenType.LPARENT) {
            ParserTreeNode exp = primaryExp.getChildren().get(1);
            ans = generateIRInstructionFromExp(exp, isLeft);
        } else if (element.getType() == SyntaxType.Token) {
            if (element.getToken().type() == TokenType.INTCON) {
                ans = new IRValue(element.getToken().value(), IRIntegerType.get32());
            } else if (element.getToken().type() == TokenType.CHARTK) {
                ans = new IRValue(element.getToken().value(), IRIntegerType.get8());
            } else {
                System.out.println("Error: generateIRInstructionFromPrimaryExp");
            }
        } else if (element.getType() == SyntaxType.LVal) {
            ans = generateIRInstructionFromLVal(element, isLeft);
        } else {
            System.out.println("Error: generateIRInstructionFromPrimaryExp");
        }
        return ans;
    }

    private IRValue generateIRInstructionFromLVal(ParserTreeNode lVal, boolean isLeft) {
        IRValue ans = null;
        ParserTreeNode element = lVal.getFirstChild();
        String name = element.getToken().value();
        Symbol symbol = this.symbolTable.getSymbol(name);
        boolean isArray = symbol.isArray();
        if (!isArray) {
            if (isLeft) {
                ans = symbol.getValue(); // 左值，直接用
            } else {
                IRValue value = symbol.getValue();
                if (!(value.getName().contains("%") || value.getName().contains("@"))) {
                    return value;
                }

                if (value.isParam()) {
                    return value;
                }

                // 局部变量，需要load
                IRValueType irValueType = value.getType();
                int cnt = functionCnt.getCnt();
                String ans_name = "%LocalVar" + cnt;
                IRLoad load = new IRLoad(irValueType, value);
                load.setName(ans_name);
                this.instructions.add(load);
                ans = load;
            }
        } else {
            if (isLeft) {
                ans = symbol.getValue().clone();
                if (lVal.hasLbrack()) {
                    ans.setDimensionValue(0);
                    ans.setDimension1Value(generateIRInstructionFromExp(lVal.getChildren().get(2), true));
                } else {
                    ans.setDimensionValue(1);
                }
            } else {
                IRValue value = symbol.getValue();
                IRValueType type = value.getType();
                ParserTreeNode exp = lVal.getChildren().get(2);
                IRValue dimension1 = generateIRInstructionFromExp(exp, false);

                String ans_name = "%LocalVar" + functionCnt.getCnt();
                IRLoad load = new IRLoad(type, value, dimension1);
                load.setName(ans_name);
                this.instructions.add(load);
                ans = load;
            }
        }
        return ans;
    }

    private void generateIRInstructionConstDecl(ParserTreeNode constDecl) {
        for (int i = 2; i < constDecl.getChildren().size(); i += 2) {
            ParserTreeNode constDef = constDecl.getChildren().get(i);
            addCon(constDef, constDecl.getChildren().get(1).getChildren().get(0).getToken().type() == TokenType.CHARTK);
        }
    }

    // 处理常量定义 (ConstDef) 并生成相应的 LLVM IR 指令
    private void addCon(ParserTreeNode constDef, boolean isChar) {
        IRIntegerType irIntegerType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
        SymbolConst symbolConst;
        String name = "%LocalConst" + functionCnt.getCnt();
        if (constDef.hasLbrack()) {
            // 数组
            if (isChar) {
                symbolConst = new SymbolConst(name, SymbolType.ConstCharArray);
            } else {
                symbolConst = new SymbolConst(name, SymbolType.ConstIntArray);
            }
            this.symbolTable.addSymbol(symbolConst);
            setConstInit(symbolConst, constDef.getLastChild(),this.symbolTable);
            IRValue value;
            value = new IRValue(name, irIntegerType);
            value.setSize(symbolConst.getValueIntArray().size());
            value.setInits1(symbolConst.getValueIntArray());
            symbolConst.setValue(value);
            // IRAlloca指令
            IRAlloca alloca = new IRAlloca(irIntegerType, value);
            alloca.setSize(value.getSize());
            alloca.setInits1(value.getInits1());
            alloca.setName(name);
            this.instructions.add(alloca);

            int i = 0;
            for (Integer init : symbolConst.getValueIntArray()) {
                IRValue irValue = new IRValue(String.valueOf(init), irIntegerType);
                this.instructions.add(new IRStore(irValue, value, 0, 1, -1, i));
                i++;
            }
        } else {
            // 单个常量
            if (isChar) {
                symbolConst = new SymbolConst(constDef.getFirstChild().getToken().value(), SymbolType.ConstChar);
            } else {
                symbolConst = new SymbolConst(constDef.getFirstChild().getToken().value(), SymbolType.ConstInt);
            }
            this.symbolTable.addSymbol(symbolConst);
            setConstInit(symbolConst, constDef.getLastChild(),this.symbolTable);
            symbolConst.setValue(new IRValue(String.valueOf(symbolConst.getValueInt()), irIntegerType));
        }
    }
}
