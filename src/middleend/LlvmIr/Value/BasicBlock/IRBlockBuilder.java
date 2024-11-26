package middleend.LlvmIr.Value.BasicBlock;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.TokenType;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Value.Function.FunctionCnt;
import middleend.LlvmIr.Value.Instruction.*;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRBr;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRGoto;
import middleend.Symbol.SymbolTable;

import java.util.ArrayList;

public class IRBlockBuilder {
    private ParserTreeNode block;
    private ParserTreeNode stmtCond;
    private ParserTreeNode stmtFor;
    private IRLabel forBegin = null;
    private IRLabel forEnd = null;

    private ArrayList<ParserTreeNode> blockItems;
    private ArrayList<IRBasicBlock> blocks;

    private SymbolTable symbolTable;
    private FunctionCnt functionCnt;

    public IRBlockBuilder(SymbolTable symbolTable,
                          FunctionCnt functionCnt,
                          IRLabel forBegin,
                          IRLabel forEnd) {
        this.symbolTable = symbolTable;
        this.functionCnt = functionCnt;
        this.forBegin = forBegin;
        this.forEnd = forEnd;
        this.blocks = new ArrayList<>();
    }

    // Block
    public IRBlockBuilder(ParserTreeNode node,
                          SymbolTable symbolTable,
                          FunctionCnt functionCnt,
                          IRLabel forBegin,
                          IRLabel forEnd) {
        this(symbolTable, functionCnt, forBegin, forEnd);
        if (node.getFirstChild().getType() == SyntaxType.Block) {
            this.block = node;
            this.blockItems = block.getBlockItems();
        } else {
            // stmt
            if (node.getFirstChild().getType() == SyntaxType.Token) {
                if (node.getFirstChild().getToken().type() == TokenType.IFTK) {
                    this.stmtCond = node;
                } else {
                    this.stmtFor = node;
                }
            } else {
                System.out.println("ERROR in IRBlockBuilder!");
            }
        }
    }

    public ArrayList<IRBasicBlock> generateIRBlocks() {
        if (this.block != null) {
            return generateIRBlockFromBlock();
        } else if (this.stmtCond != null) {
            return generateIRBlockFromCond();
        } else if (this.stmtFor != null) {
            return generateIRBlockFromFor();
        } else {
            System.out.println("ERROR in IRBlockBuilder! No block or stmt!");
        }
        return null;
    }

    private ArrayList<IRBasicBlock> generateIRBlockFromFor() {
        ParserTreeNode forInit = this.stmtFor.getForInit();
        ParserTreeNode forCond = this.stmtFor.getForCond();
        ParserTreeNode forStep = this.stmtFor.getForStep();
        ParserTreeNode stmt = this.stmtFor.getLastChild();

        // 生成forLabel,标记for循环的开始
        String name = IRLabelCnt.getName(IRLabelCnt.getCnt());
        IRLabel beginLabel = new IRLabel(name);
        IRBasicBlock block = new IRBasicBlock();
        block.addIrInstruction(beginLabel);
        this.blocks.add(block);
        // 生成ifLabel
        String ifName = IRLabelCnt.getName(IRLabelCnt.getCnt());
        IRLabel ifLabel = new IRLabel(ifName);
        // 生成endLabel
        String endName = IRLabelCnt.getName(IRLabelCnt.getCnt());
        IRLabel endLabel = new IRLabel(endName);
        // for的处理逻辑类似于if
        // 处理forInit
        IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,forInit, block, this.functionCnt, this.forBegin, this.forEnd);
        ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
        block.addAllIrInstruction(instructions);
        // 处理forCond
        generateCond(forCond, ifLabel, endLabel);
        // 处理stmt
        IRBasicBlock ifBlock = new IRBasicBlock();
        ifBlock.addIrInstruction(ifLabel);
        IRBlockBuilder blockBuilder = null;
        SymbolTable newSymbolTable = new SymbolTable(this.symbolTable);
        if (getItemType(stmt) == BlockItemType.StmtCond ||
                getItemType(stmt) == BlockItemType.StmtFor ||
                getItemType(stmt) == BlockItemType.Block) {
            this.blocks.add(ifBlock);
            if (getItemType(stmt) == BlockItemType.StmtCond) {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            } else if (getItemType(stmt) == BlockItemType.StmtFor) {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            } else {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            }
            this.blocks.addAll(blockBuilder.generateIRBlocks());
            // 添加forStep
            irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,forStep, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
            instructions = irInstructionBuilder.generateInstructions();
            ifBlock.addAllIrInstruction(instructions);
            // 添加goto
            IRGoto irGoto = new IRGoto(beginLabel);
            ifBlock.addIrInstruction(irGoto);
            this.blocks.add(ifBlock);
        } else {
            irInstructionBuilder = new IRInstructionBuilder(stmt, this.symbolTable, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
            instructions = irInstructionBuilder.generateInstructions();
            ifBlock.addAllIrInstruction(instructions);
            // 添加forStep
            irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,forStep, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
            instructions = irInstructionBuilder.generateInstructions();
            ifBlock.addAllIrInstruction(instructions);
            // 添加goto
            IRGoto irGoto = new IRGoto(beginLabel);
            ifBlock.addIrInstruction(irGoto);
            this.blocks.add(ifBlock);
        }
        // 添加endLabel
        IRBasicBlock endBlock = new IRBasicBlock();
        endBlock.addIrInstruction(endLabel);
        this.blocks.add(endBlock);
        return this.blocks;
    }

    private ArrayList<IRBasicBlock> generateIRBlockFromCond() {
        // 构造if标签
        int labelCnt = IRLabelCnt.getCnt();
        IRLabel ifLabel = new IRLabel(IRLabelCnt.getName(labelCnt));
        // 构造else标签
        int labelCntElse = -1;
        IRLabel elseLabel = null;
        // 标记是否有else
        boolean hasElse = this.stmtCond.getChildren().size() == 5;
        if (hasElse) {
            labelCntElse = IRLabelCnt.getCnt();
            elseLabel = new IRLabel(IRLabelCnt.getName(labelCntElse));
        }
        // 构造end标签
        int labelCntEnd = IRLabelCnt.getCnt();
        IRLabel endLabel = new IRLabel(IRLabelCnt.getName(labelCntEnd));

        // 处理cond
        ParserTreeNode cond = this.stmtCond.getChildren().get(2);
        if (hasElse) {
            this.blocks.addAll(generateCond(cond, ifLabel, elseLabel));
        } else {
            this.blocks.addAll(generateCond(cond, ifLabel, endLabel));
        }
        // 处理stmt
        ParserTreeNode stmt = this.stmtCond.getChildren().get(4);
        BlockItemType blockItemType = getItemType(stmt); // 只可能是stmt
        IRBasicBlock ifBlock = new IRBasicBlock();
        ifBlock.addIrInstruction(ifLabel);
        IRBlockBuilder blockBuilder = null;
        SymbolTable newSymbolTable = new SymbolTable(this.symbolTable);
        if (blockItemType == BlockItemType.StmtCond ||
                blockItemType == BlockItemType.StmtFor ||
                blockItemType == BlockItemType.Block) {
            this.blocks.add(ifBlock);
            if (blockItemType == BlockItemType.StmtCond) {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            } else if (blockItemType == BlockItemType.StmtFor) {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            } else {
                blockBuilder = new IRBlockBuilder(stmt, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
            }
            this.blocks.addAll(blockBuilder.generateIRBlocks());
            // 添加goto
            IRGoto irGoto = new IRGoto(endLabel);
            IRBasicBlock block = new IRBasicBlock();
            block.addIrInstruction(irGoto);
            this.blocks.add(block);
        } else if (blockItemType == BlockItemType.StmtSemicon) {
            // do nothing
            IRGoto irGoto = new IRGoto(endLabel);
            ifBlock.addIrInstruction(irGoto);
            this.blocks.add(ifBlock);
        } else {
            IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(stmt, this.symbolTable, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
            ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
            ifBlock.addAllIrInstruction(instructions);
            IRGoto irGoto = new IRGoto(endLabel);
            ifBlock.addIrInstruction(irGoto);
            this.blocks.add(ifBlock);
        }
        if (hasElse) {
            // 跳转到end
            ParserTreeNode stmtElse = this.stmtCond.getChildren().get(6);
            BlockItemType blockItemTypeElse = getItemType(stmtElse);
            IRBasicBlock elseBlock = new IRBasicBlock();
            // 添加else标签
            elseBlock.addIrInstruction(elseLabel);
            if (blockItemTypeElse == BlockItemType.StmtCond ||
                    blockItemTypeElse == BlockItemType.StmtFor ||
                    blockItemTypeElse == BlockItemType.Block) {
                this.blocks.add(elseBlock);
                newSymbolTable = new SymbolTable(this.symbolTable);
                if (blockItemTypeElse == BlockItemType.StmtCond) {
                    blockBuilder = new IRBlockBuilder(stmtElse, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
                } else if (blockItemTypeElse == BlockItemType.StmtFor) {
                    blockBuilder = new IRBlockBuilder(stmtElse, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
                } else {
                    blockBuilder = new IRBlockBuilder(stmtElse, newSymbolTable, this.functionCnt, this.forBegin, this.forEnd);
                }
                this.blocks.addAll(blockBuilder.generateIRBlocks());
                // 添加goto
                IRGoto irGoto = new IRGoto(endLabel);
                IRBasicBlock block = new IRBasicBlock();
                block.addIrInstruction(irGoto);
                this.blocks.add(block);
            } else if (blockItemTypeElse == BlockItemType.StmtSemicon) {
                // do nothing
                IRGoto irGoto = new IRGoto(endLabel);
                elseBlock.addIrInstruction(irGoto);
                this.blocks.add(elseBlock);
            } else {
                IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(stmtElse, this.symbolTable, elseBlock, this.functionCnt, this.forBegin, this.forEnd);
                ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
                elseBlock.addAllIrInstruction(instructions);
                IRGoto irGoto = new IRGoto(endLabel);
                elseBlock.addIrInstruction(irGoto);
                this.blocks.add(elseBlock);
            }
        }
        // 添加end标签
        IRBasicBlock endBlock = new IRBasicBlock();
        endBlock.addIrInstruction(endLabel);
        this.blocks.add(endBlock);
        return this.blocks;
    }

    private ArrayList<IRBasicBlock> generateCond(ParserTreeNode cond, IRLabel ifLabel, IRLabel endLable) {
        // 处理cond
        IRBasicBlock block = new IRBasicBlock();

        ParserTreeNode LOrExp = cond.getFirstChild();
        // 处理LOrExp
        ArrayList<ParserTreeNode> landExps = LOrExp.getLAndExps();
        for (int i = 0; i < landExps.size(); i++) {
            IRLabel nextLabel = generateIRInstructionFromLandExp(landExps.get(i), ifLabel);
            if (nextLabel != null) {
                IRBasicBlock block1 = new IRBasicBlock();
                block1.addIrInstruction(nextLabel);
                this.blocks.add(block1);
            }
        }
        IRGoto irGoto = new IRGoto(endLable);
        block.addIrInstruction(irGoto);
        this.blocks.add(block);
        return new ArrayList<>();
    }

    private IRLabel generateIRInstructionFromLandExp(ParserTreeNode landExp, IRLabel ifLabel) {
        ArrayList<ParserTreeNode> eqExps = landExp.getEqExps();
        IRLabel ans = null;
        if (eqExps.size() == 1) {
            generateIRInstructionFromEqExp(eqExps.get(0), ifLabel, true);
        } else {
            // 说有多个eqExp
            String name = IRLabelCnt.getName(IRLabelCnt.getCnt());
            ans = new IRLabel(name);
            for (int i = 0; i < eqExps.size(); i++) {
                generateIRInstructionFromEqExp(eqExps.get(i), ans, false);
            }
            IRBasicBlock block = new IRBasicBlock();
            IRGoto irGoto = new IRGoto(ifLabel);
            block.addIrInstruction(irGoto);
            this.blocks.add(block);
        }
        return ans;
    }

    private void generateIRInstructionFromEqExp(ParserTreeNode eqexp, IRLabel ifLable, boolean pos) {
        ArrayList<ParserTreeNode> relExps = eqexp.getRelExps();
        ArrayList<TokenType> relops = eqexp.getRelops();

        IRValue left = generateIRInstructionFromRelExp(relExps.get(0));
        IRValue right = null;

        for (int i = 1; i < relExps.size(); i++) {
            right = generateIRInstructionFromRelExp(relExps.get(i));
            // 生成比较指令
            IRBinaryInstruction instruction;
            if (relops.get(i - 1) == TokenType.EQL) {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(), IRInstructionType.Eq, left, right);
            } else {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(), IRInstructionType.Ne, left, right);
            }
            String name = "%LocalVar" + this.functionCnt.getCnt();
            left = new IRValue(name, IRIntegerType.get32());
            instruction.setName(name);
            // store
            IRBasicBlock basicBlock = new IRBasicBlock();
            basicBlock.addIrInstruction(instruction);
            this.blocks.add(basicBlock);
        }
        if (relExps.size() > 1) {
            right = generateIRInstructionFromRelExp(relExps.get(relExps.size() - 1));
        }
        // 生成Branch指令
        IRBr irBr;
        if (pos) {
            // 正常
            if(relops.isEmpty()) {
                // 无relops,只有一个relExp
                IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder();
                IRValue zero = irInstructionBuilder.generateZero();
                irBr = new IRBr(left, zero, ifLable, IRInstructionType.Bne);
            } else if (relops.get(relops.size() - 1) == TokenType.EQL) {
                irBr = new IRBr(left, right, ifLable, IRInstructionType.Beq);
            } else {
                irBr = new IRBr(left, right, ifLable, IRInstructionType.Bne);
            }
        } else {
            // 取反
            if (relops.isEmpty()) {
                // 无relops,只有一个relExp
                IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder();
                IRValue zero = irInstructionBuilder.generateZero();
                irBr = new IRBr(left, zero, ifLable, IRInstructionType.Beq);
            } else if (relops.get(relops.size() - 1) == TokenType.EQL) {
                irBr = new IRBr(left, right, ifLable, IRInstructionType.Bne);
            } else {
                irBr = new IRBr(left, right, ifLable, IRInstructionType.Beq);
            }
        }
        IRBasicBlock block1 = new IRBasicBlock();
        block1.addIrInstruction(irBr);
        this.blocks.add(block1);
    }

    private IRValue generateIRInstructionFromRelExp(ParserTreeNode relExp) {
        ArrayList<ParserTreeNode> addExps = relExp.getAddExps();
        ArrayList<TokenType> addops = relExp.getAddops();
        int length = addExps.size();
        IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,new IRBasicBlock(),addExps.get(0),this.functionCnt,this.forBegin,this.forEnd);
        IRBasicBlock basicBlock = new IRBasicBlock();
        ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
        basicBlock.addAllIrInstruction(instructions);

        IRValue left = irInstructionBuilder.getLeft();
        IRValue right;
        for (int i = 1; i < length; i++) {
            irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,new IRBasicBlock(),addExps.get(i),this.functionCnt,this.forBegin,this.forEnd);
            instructions = irInstructionBuilder.generateInstructions();
            basicBlock.addAllIrInstruction(instructions);
            right = irInstructionBuilder.getLeft();
            // 生成比较指令
            IRBinaryInstruction instruction = null;
            TokenType addop = addops.get(i - 1);
            if (addop == TokenType.LSS) {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(),IRInstructionType.Lt, left, right);
            } else if (addop == TokenType.LEQ) {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(),IRInstructionType.Le, left, right);
            } else if (addop == TokenType.GRE) {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(),IRInstructionType.Gt, left, right);
            } else if (addop == TokenType.GEQ) {
                instruction = new IRBinaryInstruction(IRIntegerType.get32(),IRInstructionType.Ge, left, right);
            } else {
                System.out.println("ERROR in generateIRInstructionFromRelExp!");
            }
            String name = "%LocalVar" + this.functionCnt.getCnt();
            left = new IRValue(name,IRIntegerType.get32());
            instruction.setName(name);
            basicBlock.addIrInstruction(instruction);
        }
        this.blocks.add(basicBlock);
        return left;
    }

    private enum BlockItemType {
        StmtCond,
        StmtFor,
        Block,
        // 上面三个要递归处理
        ConstDecl,
        VarDecl,
        StmtAssign,
        StmtBreak,
        StmtContinue,
        StmtReturn,
        StmtExp,
        StmtInputInt,
        StmtInputChar,
        StmtOutput,
        StmtSemicon // 即空语句
        // 上面这些不需要递归处理
    }

    private BlockItemType getItemType(ParserTreeNode node) {
        if (node.getType() == SyntaxType.Decl) {
            if (node.getFirstChild().getType() == SyntaxType.ConstDecl) {
                return BlockItemType.ConstDecl;
            } else {
                return BlockItemType.VarDecl;
            }
        } else {
            // Stmt
            ParserTreeNode stmt = node.getFirstChild();
            if (stmt.getFirstChild().getType() == SyntaxType.Token) {
                TokenType tokenType = stmt.getFirstChild().getToken().type();
                if (tokenType == TokenType.IFTK) {
                    return BlockItemType.StmtCond;
                } else if (tokenType == TokenType.FORTK) {
                    return BlockItemType.StmtFor;
                } else if (tokenType == TokenType.BREAKTK) {
                    return BlockItemType.StmtBreak;
                } else if (tokenType == TokenType.CONTINUETK) {
                    return BlockItemType.StmtContinue;
                } else if (tokenType == TokenType.RETURNTK) {
                    return BlockItemType.StmtReturn;
                } else {
                    // tokenType == TokenType.SEMICN
                    return BlockItemType.StmtSemicon;
                }
            } else if (stmt.getFirstChild().getType() == SyntaxType.LVal) {
                if (stmt.getChildren().get(2).getType() == SyntaxType.Token) {
                    TokenType tokenType = stmt.getChildren().get(2).getToken().type();
                    if (tokenType == TokenType.GETINTTK) {
                        return BlockItemType.StmtInputInt;
                    } else {
                        return BlockItemType.StmtInputChar;
                    }
                } else {
                    return BlockItemType.StmtAssign;
                }
            } else if (stmt.getFirstChild().getType() == SyntaxType.Block) {
                return BlockItemType.Block;
            } else if (stmt.getFirstChild().getType() == SyntaxType.Exp) {
                return BlockItemType.StmtExp;
            } else {
                return BlockItemType.StmtOutput;
            }
        }
    }

    // Block
    private ArrayList<IRBasicBlock> generateIRBlockFromBlock() {
        int len = this.blockItems.size();
        int pointer = 0;
        while (pointer < len) {
            ParserTreeNode blockItem = this.blockItems.get(pointer).getFirstChild();
            BlockItemType blockItemType = getItemType(blockItem);
            if (blockItemType.ordinal() <= BlockItemType.Block.ordinal()) {
                // 递归处理
                SymbolTable symbolTableSon = new SymbolTable(this.symbolTable);
                ParserTreeNode stmt = blockItem.getFirstChild();
                IRBlockBuilder blockBuilder = new IRBlockBuilder(stmt, symbolTableSon, this.functionCnt, this.forBegin, this.forEnd);
                this.blocks.addAll(blockBuilder.generateIRBlocks());
                pointer++;
            } else if (blockItemType.ordinal() < BlockItemType.StmtSemicon.ordinal()) {
                // 不需要递归处理
                IRBasicBlock block = new IRBasicBlock();
                while (pointer < this.blockItems.size()) {
                    ParserTreeNode blockItemNow = this.blockItems.get(pointer).getFirstChild();
                    BlockItemType blockItemTypeNow = getItemType(blockItemNow);
                    if (blockItemTypeNow.ordinal() <= BlockItemType.Block.ordinal()) {
                        break;
                    } else if (blockItemTypeNow == BlockItemType.StmtSemicon) {
                        pointer++;
                        continue;
                    }
                    IRInstructionBuilder irInstructionBuilder
                            = new IRInstructionBuilder(blockItemNow, this.symbolTable, block, this.functionCnt, forBegin,forEnd);
                    ArrayList<IRInstruction> temp = irInstructionBuilder.generateInstructions();
                    block.addAllIrInstruction(temp);
                    pointer++;
                }
                this.blocks.add(block);
                continue;
            } else {
                // StmtSemicon
                // do nothing
                pointer++;
            }
        }
        return this.blocks;
    }
}
