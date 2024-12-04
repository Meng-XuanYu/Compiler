package middleend.LlvmIr.Value.Instruction;

import frontend.Parser.ParserTreeNode;
import frontend.SymbolParser.SymbolType;
import frontend.SyntaxType;
import frontend.TokenType;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRFunctionType;
import middleend.LlvmIr.Types.IRIntArrayType;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.Constant.IRConstantInt;
import middleend.LlvmIr.Value.Constant.IRConstantIntArray;
import middleend.LlvmIr.Value.Function.FunctionCnt;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.*;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRCall;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRGoto;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRRet;
import middleend.Symbol.*;

import java.util.ArrayList;
import java.util.Locale;

import static middleend.LlvmIr.Value.GlobalVar.IRGlobalVarBuilder.setConstInit;
import static middleend.LlvmIr.Value.GlobalVar.IRGlobalVarBuilder.setVarInit;

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
            if (this.blockItem.getFirstChild().getType() == SyntaxType.Decl) {
                ParserTreeNode decl = blockItem.getFirstChild();
                if (decl.getFirstChild().getType() == SyntaxType.ConstDecl) {
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
                    } else if (tokenType == TokenType.SEMICN) {
                        // tokenType == TokenType.SEMICN
                    } else {
                        this.stmtOutput = stmt;
                        generateIRinstructionFromStmtOutput();
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
                } else  {
                    //if (stmt.getFirstChild().getType() == SyntaxType.Exp)
                    this.stmtExp = stmt;
                    generateIRInstructionFromStmtExp();
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
                } else if (tokenType == TokenType.SEMICN) {
                    // tokenType == TokenType.SEMICN
                } else {
                    this.stmtOutput = stmt;
                    generateIRinstructionFromStmtOutput();
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
            } else  {
                // if (stmt.getFirstChild().getType() == SyntaxType.Exp)
                this.stmtExp = stmt;
                generateIRInstructionFromStmtExp();
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
                if (value.isParam()) {
                    IRAlloca alloca = new IRAlloca(value.getType(), value);
                    alloca.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(alloca);
                    IRStore store = new IRStore(value, alloca, false);
                    this.instructions.add(store);
                    IRLoad load = new IRLoad(value.getType(), alloca);
                    load.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(load);
                    value = load;
                    value.setValueType(IRIntegerType.get32());
                }
                call = new IRCall("@putint", value);
                i++;
                cnt++;
            } else if (c == '\\' && i + 1 < chars.length && chars[i + 1] == 'n') {
                call = new IRCall("@putch", '\n');
                i++;
            } else if (c == '%' && i + 1 < chars.length && chars[i + 1] == 'c') {
                IRValue value = values.get(cnt);
                IRIntegerType irIntegerType = (IRIntegerType) value.getType();
                if (value.isParam()) {
                    IRAlloca alloca = new IRAlloca(value.getType(), value);
                    alloca.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(alloca);
                    IRStore store = new IRStore(value, alloca, true);
                    this.instructions.add(store);
                    IRLoad load = new IRLoad(value.getType(), alloca);
                    load.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(load);
                    value = load;
                    value.setValueType(IRIntegerType.get8());
                }
                if (irIntegerType.getBitWidth() == 8) {
                    IRZext zext = new IRZext(value, IRIntegerType.get32());
                    zext.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(zext);
                    value = zext;
                }
                call = new IRCall("@putch", value);
                i++;
                cnt++;
            }  else {
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

        IRTrunc trunc = new IRTrunc(call, IRIntegerType.get8());
        trunc.setName("%LocalVar" + functionCnt.getCnt());
        this.instructions.add(trunc);


        IRStore store;
        if (left.getSize() == 0) {
            store = new IRStore(trunc, left, true);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(lVal.getChildren().get(2), false);
            store = new IRStore(trunc, left, 0, 1, null, dimension1PointerValue,true);
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
            store = new IRStore(call, left,false);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(lVal.getChildren().get(2), false);
            store = new IRStore(call, left, 0, 1, null, dimension1PointerValue,false);
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
            IRFunction function = (IRFunction) this.block.getParentFunction();
            IRValueType returnType = ((IRFunctionType) function.getType()).getReturnType();
            if (returnType instanceof IRIntegerType) {
                IRIntegerType irIntegerType = (IRIntegerType) returnType;
                if (irIntegerType.getBitWidth() == 8 && value.getType().isInt32()) {
                    IRTrunc trunc = new IRTrunc(value, IRIntegerType.get8());
                    trunc.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(trunc);
                    value = trunc;
                } else if (irIntegerType.getBitWidth() == 32 && !value.getType().isInt32()) {
                    IRZext zext = new IRZext(value, IRIntegerType.get32());
                    zext.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(zext);
                    value = zext;
                }
            }
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
        String name = lVal.getFirstChild().getToken().value();
        boolean isChar = symbolTable.getSymbol(name).isChar();

        IRValue left = generateIRInstructionFromLVal(lVal, true);
        IRValue right = generateIRInstructionFromExp(exp, false);
        int leftSize = left.getSize();
        int rightSize = right.getSize();

        IRStore store;
        if (leftSize == 0) {
            store = new IRStore(right, left, isChar);
        } else {
            IRValue dimension1PointerValue = generateIRInstructionFromExp(exp, false);
            store = new IRStore(right, left, rightSize, leftSize, null, dimension1PointerValue, isChar);
        }
        this.instructions.add(store);
    }

    private void generateIRInstructionVarDecl(ParserTreeNode varDecl) {
        for (int i = 1; i < varDecl.getChildren().size(); i += 2) {
            ParserTreeNode varDef = varDecl.getChildren().get(i);
            addVar(varDef, varDecl.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.CHARTK);
        }
    }

    private void addVar(ParserTreeNode varDef, boolean isChar) {
        SymbolVar symbol;
        String name = "%LocalVar" + functionCnt.getCnt();
        if (varDef.varDefHasAssign()) {
            ParserTreeNode varDefInit = varDef.getLastChild();
            if (varDef.hasLbrack()) {
                int size = varDef.getArraySize(this.symbolTable);
                IRIntArrayType irIntegerType = isChar ? new IRIntArrayType(IRIntegerType.get8(),size) :
                        new IRIntArrayType(IRIntegerType.get32(),size);
                IRValueType irValueType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
                // 数组
                if (isChar) {
                    symbol = new SymbolVar(varDef.getFirstChild().getToken().value(), SymbolType.CharArray);
                } else {
                    symbol = new SymbolVar(varDef.getFirstChild().getToken().value(), SymbolType.IntArray);
                }
                this.symbolTable.addSymbol(symbol);
                setVarInit(symbol, varDef.getLastChild(),this.symbolTable);
                IRValue value = new IRValue(name, irIntegerType);
                value.setSize(size);
                symbol.setValue(value);

                // IRAlloca指令
                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setSize(value.getSize());
                alloca.setName(name);
                this.instructions.add(alloca);

                IRGetElementPtr firstPtr = new IRGetElementPtr(irValueType, alloca, new IRConstantInt(0, irValueType));
                String name1 = "%LocalVar" + functionCnt.getCnt();
                firstPtr.setName(name1);
                this.instructions.add(firstPtr);
                symbol.setIns(firstPtr);
                symbol.setValue(firstPtr);

                if (varDefInit.getFirstChild().getType() == SyntaxType.Token && varDefInit.getFirstChild().getToken().type() == TokenType.STRCON) {
                    // 字符串
                    ArrayList<Integer> initVal = symbol.getInitValArray();
                    for (int i = 0; i < initVal.size(); i++) {
                        IRValue initValue = new IRValue(String.valueOf(initVal.get(i)), irIntegerType);
                        IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, new IRConstantInt(i, irValueType));
                        String name2 = "%LocalVar" + functionCnt.getCnt();
                        getPtr.setName(name2);
                        this.instructions.add(getPtr);
                        IRStore store = new IRStore(initValue, getPtr);
                        store.setName(name2);
                        this.instructions.add(store);
                    }
                } else {
                    // 表达式{}
                    ArrayList<Integer> initVal = symbol.getInitValArray();
                    for (int i = 0; i < initVal.size(); i++) {
                        IRValue initValue = new IRValue(String.valueOf(initVal.get(i)), irIntegerType);
                        IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, new IRConstantInt(i, irValueType));
                        String name2 = "%LocalVar" + functionCnt.getCnt();
                        getPtr.setName(name2);
                        this.instructions.add(getPtr);
                        IRStore store = new IRStore(initValue, getPtr);
                        store.setName(name2);
                        this.instructions.add(store);
                    }
                }
            } else {
                IRIntegerType irIntegerType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
                // 单个变量
                IRValue value = new IRValue(name, irIntegerType);
                value.setName(name);

                SymbolType symbolType = isChar ? SymbolType.Char : SymbolType.Int;
                SymbolVar symbolVar = new SymbolVar(varDef.getFirstChild().getToken().value(), symbolType, value);
                this.symbolTable.addSymbol(symbolVar);

                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setName(name);
                alloca.setSize(0);
                this.instructions.add(alloca);

                ParserTreeNode initVal = varDef.getLastChild();
                // 一定是表达式
                ParserTreeNode exp = initVal.getFirstChild();
                IRValue right = generateIRInstructionFromExp(exp, false);
                IRStore store = new IRStore(right, value, isChar);
                this.instructions.add(store);
            }
        } else {
            // 无初始化
            if (varDef.hasLbrack()) {
                int size = varDef.getArraySize(this.symbolTable);
                IRIntArrayType irIntegerType = isChar ? new IRIntArrayType(IRIntegerType.get8(),size) :
                        new IRIntArrayType(IRIntegerType.get32(),size);
                IRValueType irValueType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
                // 数组
                if (isChar) {
                    symbol = new SymbolVar(varDef.getFirstChild().getToken().value(), SymbolType.CharArray);
                } else {
                    symbol = new SymbolVar(varDef.getFirstChild().getToken().value(), SymbolType.IntArray);
                }
                this.symbolTable.addSymbol(symbol);
                setVarInit(symbol, null,this.symbolTable);
                IRValue value = new IRValue(name, irIntegerType);
                value.setSize(varDef.getChildren().get(2).getChildren().size());
                symbol.setValue(value);

                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setName(name);
                alloca.setSize(value.getSize());
                this.instructions.add(alloca);

                IRGetElementPtr firstPtr = new IRGetElementPtr(irValueType, alloca, new IRConstantInt(0, irValueType));
                String name1 = "%LocalVar" + functionCnt.getCnt();
                firstPtr.setName(name1);
                this.instructions.add(firstPtr);
                symbol.setIns(firstPtr);
            } else {
                IRIntegerType irIntegerType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
                IRValue value = new IRValue(name, irIntegerType);
                value.setName(name);

                SymbolType symbolType = isChar ? SymbolType.Char : SymbolType.Int;
                SymbolVar symbolVar = new SymbolVar(varDef.getFirstChild().getToken().value(), symbolType, value);
                this.symbolTable.addSymbol(symbolVar);

                IRAlloca alloca = new IRAlloca(irIntegerType, value);
                alloca.setName(name);
                alloca.setSize(0);
                this.instructions.add(alloca);
            }
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
                IRIntegerType irIntegerTypeLeft = (IRIntegerType) left.getType();

                if (irIntegerTypeLeft.getBitWidth() == 8) {
                    IRZext zext = new IRZext(left, IRIntegerType.get32());
                    zext.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(zext);
                    left = zext;
                }
                boolean rightIsChar = !right.getType().isInt32();
                if (rightIsChar) {
                    IRZext zext = new IRZext(right, IRIntegerType.get32());
                    zext.setName("%LocalVar" + functionCnt.getCnt());
                    this.instructions.add(zext);
                    right = zext;
                }

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
        TokenType type = unaryOp.getFirstChild().getToken().type();
        if (type == TokenType.PLUS) {
            ans = generateIRInstructionFromUnaryExp(unaryExp_child, isLeft);
        } else if (type == TokenType.MINU) {
            IRValue left = new IRValue("-1", IRIntegerType.get32());
            IRBinaryInstruction mulExp = new IRBinaryInstruction(IRIntegerType.get32(),
                    IRInstructionType.Mul, left, generateIRInstructionFromUnaryExp(unaryExp_child, isLeft));
            String name = "%LocalVar" + functionCnt.getCnt();
            mulExp.setName(name);
            this.instructions.add(mulExp);
            ans = mulExp;
        } else if (type == TokenType.NOT) {
            IRBinaryInstruction not = new IRBinaryInstruction(IRIntegerType.get1(),
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
                    arg = generateIRInstructionFromExp(exp, false);
                } else {
                    arg = generateIRInstructionFromExp(exp, false);
                }
                args.add(arg);
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
            return call;
        }
    }

    private IRValue generateIRInstructionFromPrimaryExp(ParserTreeNode primaryExp, boolean isLeft) {
        IRValue ans = null;
        ParserTreeNode element = primaryExp.getFirstChild();
        if (element.getType() == SyntaxType.Token && element.getToken().type() == TokenType.LPARENT) {
            ParserTreeNode exp = primaryExp.getChildren().get(1);
            ans = generateIRInstructionFromExp(exp, isLeft);
        } else if (element.getType() == SyntaxType.Number) {
            ans = new IRValue(element.getFirstChild().getToken().value(), IRIntegerType.get32());
        } else if (element.getType() == SyntaxType.LVal) {
            ans = generateIRInstructionFromLVal(element, isLeft);
        } else if (element.getType() == SyntaxType.Character){
            String name = ((int)element.getFirstChild().getToken().value().charAt(1)) + "";
            ans = new IRValue(name, IRIntegerType.get8());
        } else {
            System.out.println("Error: generateIRInstructionFromPrimaryExp");
        }
        return ans;
    }

    private IRValue generateIRInstructionFromLVal(ParserTreeNode lVal, boolean isLeft) {
        IRValue ans;
        ParserTreeNode element = lVal.getFirstChild();
        String name = element.getToken().value();
        Symbol symbol = this.symbolTable.getSymbol(name);
        boolean isArray = symbol.isArray();
        IRValue value = symbol.getValue();
        if (!isArray) {
            // 标识符不是数组
            if (isLeft) {
                // 左值，如果是第一次使用参数，如果是一维则需要alloca
                if (value.isParam()) {
                    IRValueType irValueType = value.getType();
                    int cnt = functionCnt.getCnt();
                    String name_temp = "%LocalVar" + cnt;
                    IRValue newValue = new IRValue(name_temp, irValueType);
                    symbol.setValue(newValue);

                    IRAlloca alloca = new IRAlloca(irValueType, newValue);
                    alloca.setName(name_temp);
                    this.instructions.add(alloca);
                    boolean isChar = symbol.isChar();
                    IRStore store = new IRStore(value,newValue,isChar);
                    store.setName(name_temp);
                    this.instructions.add(store);
                    return newValue;
                } else {
                    return symbol.getValue();
                }
            } else {
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
                // 如果是二维则需要getelementptr,且一定有[]
                ParserTreeNode exp = lVal.getChildren().get(2);
                IRValue dimension1 = generateIRInstructionFromExp(exp, false);
                IRValueType irIntegerType = ((IRIntArrayType)value.getType()).getType();

                IRValue firstPtr;
                if (symbol instanceof SymbolVar) {
                    firstPtr = ((SymbolVar)symbol).getIns();
                } else {
                    firstPtr = ((SymbolConst)symbol).getIns();
                    System.out.println("Error: generateIRInstructionFromLVal");
                }
                if (firstPtr == null) {
                    // 说明是全局变量,直接取用
                    IRAlloca alloca = new IRAlloca(value.getType(), value);
                    alloca.setName(value.getName());
                    firstPtr = alloca;
                }
                IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, dimension1);
                String ans_name = "%LocalVar" + functionCnt.getCnt();
                getPtr.setName(ans_name);
                this.instructions.add(getPtr);
                ans = getPtr;

                getPtr.setDimensionValue(0);
                getPtr.setDimension1Value(dimension1);
            } else {
                // 不是左值可能是a[1],和a
                if (lVal.hasLbrack()) {
                    ParserTreeNode exp = lVal.getChildren().get(2);
                    IRValue dimension1 = generateIRInstructionFromExp(exp, false);
                    IRValueType irIntegerType = ((IRIntArrayType)value.getType()).getType();
                    String ans_name = "%LocalVar" + functionCnt.getCnt();
                    IRValue firstPtr;
                    if (symbol instanceof SymbolVar) {
                        firstPtr = ((SymbolVar)symbol).getIns();
                    } else {
                        firstPtr = ((SymbolConst)symbol).getIns();
                    }
                    if (firstPtr == null) {
                        // 说明是全局变量
                        IRAlloca alloca = new IRAlloca(value.getType(), value);
                        alloca.setName(value.getName());
                        firstPtr = alloca;
                    }
                    IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, dimension1);
                    getPtr.setName(ans_name);
                    this.instructions.add(getPtr);
                    IRLoad load = new IRLoad(irIntegerType, getPtr);
                    String name_2 = "%LocalVar" + functionCnt.getCnt();
                    load.setName(name_2);
                    this.instructions.add(load);
                    ans = load;

                    getPtr.setDimensionValue(0);
                    getPtr.setDimension1Value(dimension1);
                } else {
                    // 直接传递数组名
                    ans = value;
                    ans.setDimensionValue(1);
                }
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
        SymbolConst symbolConst;
        String name = "%LocalConst" + functionCnt.getCnt();
        if (constDef.hasLbrack()) {
            IRValueType irIntegerType = isChar ? new IRIntArrayType(IRIntegerType.get8(),constDef.getArraySize(this.symbolTable)) :
                    new IRIntArrayType(IRIntegerType.get32(),constDef.getArraySize(this.symbolTable));
            IRValueType irValueType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
            // 数组
            if (isChar) {
                symbolConst = new SymbolConst(constDef.getFirstChild().getToken().value(), SymbolType.ConstCharArray);
            } else {
                symbolConst = new SymbolConst(constDef.getFirstChild().getToken().value(), SymbolType.ConstIntArray);
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

            // 初始化
            IRGetElementPtr firstPtr = new IRGetElementPtr(irValueType, alloca, new IRConstantInt(0, IRIntegerType.get32()));
            String name1 = "%LocalVar" + functionCnt.getCnt();
            firstPtr.setName(name1);
            this.instructions.add(firstPtr);
            symbolConst.setIns(firstPtr);

            ArrayList<Integer> initVal = symbolConst.getValueIntArray();
            if (isChar) {
                for (int i = 0; i < initVal.size(); i++) {
                    IRValue initValue = new IRValue(String.valueOf((char)initVal.get(i).intValue()), irIntegerType);
                    IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, new IRConstantInt(i, IRIntegerType.get32()));
                    String name2 = "%LocalVar" + functionCnt.getCnt();
                    getPtr.setName(name2);
                    this.instructions.add(getPtr);
                    IRStore store = new IRStore(initValue, getPtr);
                    store.setName(name2);
                    this.instructions.add(store);
                }
            } else {
                for (int i = 0; i < initVal.size(); i++) {
                    IRValue initValue = new IRValue(String.valueOf(initVal.get(i)), irIntegerType);
                    IRGetElementPtr getPtr = new IRGetElementPtr(irIntegerType, firstPtr, new IRConstantInt(i, IRIntegerType.get32()));
                    String name2 = "%LocalVar" + functionCnt.getCnt();
                    getPtr.setName(name2);
                    this.instructions.add(getPtr);
                    IRStore store = new IRStore(initValue, getPtr);
                    store.setName(name2);
                    this.instructions.add(store);
                }
            }
        } else {
            IRIntegerType irIntegerType = isChar ? IRIntegerType.get8() : IRIntegerType.get32();
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
