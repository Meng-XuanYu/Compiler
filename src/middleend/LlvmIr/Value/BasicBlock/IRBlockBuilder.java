package middleend.LlvmIr.Value.BasicBlock;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Value.Function.FunctionCnt;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.Instruction.*;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRZext;
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
    private IRFunction parentFunction;

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
                          IRLabel forEnd,
                          IRFunction parentFunction) {
        this(symbolTable, functionCnt, forBegin, forEnd);
        this.block = node;
        this.blockItems = block.getBlockItems();
        this.parentFunction = parentFunction;
    }


    public IRBlockBuilder(ParserTreeNode node,
                          SymbolTable symbolTable,
                          FunctionCnt functionCnt,
                          IRLabel forBegin,
                          IRLabel forEnd) {
        this(symbolTable, functionCnt, forBegin, forEnd);
        if (node.getType() == SyntaxType.Block) {
            System.out.println("ERROR in IRBlockBuilder! block");
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
        if (forInit != null) {
            IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,forInit, block, this.functionCnt, this.forBegin, this.forEnd);
            ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
            block.addAllIrInstruction(instructions);
        }
        // 处理forCond
        if (forCond != null) {
            generateCond(forCond, ifLabel, endLabel);
        }
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
                stmt = stmt.getFirstChild();
                blockBuilder = new IRBlockBuilder(stmt.getFirstChild(), newSymbolTable, this.functionCnt, this.forBegin, this.forEnd, this.parentFunction);
            }
            this.blocks.addAll(blockBuilder.generateIRBlocks());
            // 添加forStep
            if (forStep != null) {
                IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable,forStep, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
                ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
                ifBlock.addAllIrInstruction(instructions);
            }
            // 添加goto
            IRGoto irGoto = new IRGoto(beginLabel);
            ifBlock.addIrInstruction(irGoto);
            this.blocks.add(ifBlock);
        } else {
            IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable, stmt, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
            ArrayList<IRInstruction> instructions = irInstructionBuilder.generateInstructions();
            ifBlock.addAllIrInstruction(instructions);
            // 添加forStep
            if (forStep != null) {
                IRInstructionBuilder irInstructionBuilder1 = new IRInstructionBuilder(this.symbolTable,forStep, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
                ArrayList<IRInstruction> instructions1 = irInstructionBuilder1.generateInstructions();
                ifBlock.addAllIrInstruction(instructions1);
            }
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
        boolean hasElse = this.stmtCond.getChildren().size() != 5;
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
            generateCond(cond, ifLabel, elseLabel);
        } else {
            generateCond(cond, ifLabel, endLabel);
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
                blockBuilder = new IRBlockBuilder(stmt.getFirstChild(), newSymbolTable, this.functionCnt, this.forBegin, this.forEnd, this.parentFunction);
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
            IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable, stmt, ifBlock, this.functionCnt, this.forBegin, this.forEnd);
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
                    blockBuilder = new IRBlockBuilder(stmtElse.getFirstChild(), newSymbolTable, this.functionCnt, this.forBegin, this.forEnd, this.parentFunction);
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
                IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder(this.symbolTable, stmtElse, elseBlock, this.functionCnt, this.forBegin, this.forEnd);
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

    private void generateCond(ParserTreeNode cond, IRLabel ifLabel, IRLabel endLabel) {
        // 处理cond
        ParserTreeNode LOrExp = cond.getFirstChild();
        // 处理LOrExp
        IRLabel ans;
        ArrayList<ParserTreeNode> landExps = LOrExp.getLAndExps();
        if (landExps.size() == 1) {
            generateIRInstructionFromLandExp(landExps.get(0), ifLabel, endLabel);
        } else {
            for (int i = 0; i < landExps.size() - 1; i++) {
                IRBasicBlock block = new IRBasicBlock();
                String name = IRLabelCnt.getName(IRLabelCnt.getCnt());
                ans = new IRLabel(name);
                generateIRInstructionFromLandExp(landExps.get(i), ifLabel, ans);
                block.addIrInstruction(ans);
                this.blocks.add(block);
            }
            generateIRInstructionFromLandExp(landExps.get(landExps.size() - 1), ifLabel,endLabel);
        }
    }

    private void generateIRInstructionFromLandExp(ParserTreeNode landExp, IRLabel ifLabel, IRLabel endLabel) {
        ArrayList<ParserTreeNode> eqExps = landExp.getEqExps();
        IRLabel ans;
        if (eqExps.size() == 1) {
            generateIRInstructionFromEqExp(eqExps.get(0), ifLabel,endLabel, true);
        } else {
            for (int i = 0; i < eqExps.size() - 1; i++) {
                IRBasicBlock block = new IRBasicBlock();
                String name = IRLabelCnt.getName(IRLabelCnt.getCnt());
                ans = new IRLabel(name);
                generateIRInstructionFromEqExp(eqExps.get(i), ans ,endLabel, true);
                block.addIrInstruction(ans);
                this.blocks.add(block);
            }
            generateIRInstructionFromEqExp(eqExps.get(eqExps.size() - 1), ifLabel,endLabel, true);
        }
    }

    private void generateIRInstructionFromEqExp(ParserTreeNode eqexp, IRLabel ifLable,IRLabel endLabel, boolean pos) {
        ArrayList<ParserTreeNode> relExps = eqexp.getRelExps();
        ArrayList<TokenType> relops = eqexp.getRelops();

        IRValue left = generateIRInstructionFromRelExp(relExps.get(0));
        IRValue right;

        for (int i = 1; i < relExps.size(); i++) {
            right = generateIRInstructionFromRelExp(relExps.get(i));
            // 生成比较指令
            IRBinaryInstruction instruction;
            if (relops.get(i - 1) == TokenType.EQL) {
                instruction = new IRBinaryInstruction(IRIntegerType.get1(), IRInstructionType.Eq, left, right);
            } else {
                instruction = new IRBinaryInstruction(IRIntegerType.get1(), IRInstructionType.Ne, left, right);
            }
            String name = "%LocalVar" + this.functionCnt.getCnt();
            left = new IRValue(name, IRIntegerType.get1());
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
        if (left.getType() instanceof IRIntegerType) {
            int bitWidth = ((IRIntegerType) left.getType()).getBitWidth();
            if (bitWidth != 32) {
                IRZext irZext = new IRZext(left, IRIntegerType.get32());
                String name = "%LocalVar" + this.functionCnt.getCnt();
                irZext.setName(name);
                IRBasicBlock block = new IRBasicBlock();
                block.addIrInstruction(irZext);
                this.blocks.add(block);
                left = irZext;
            }
        }
        IRInstructionBuilder irInstructionBuilder = new IRInstructionBuilder();
        IRValue zero = irInstructionBuilder.generateZero();
        IRInstruction instruction = new IRBinaryInstruction(IRIntegerType.get1(), IRInstructionType.Ne, left, zero);
        String name = "%LocalVar" + this.functionCnt.getCnt();
        left = new IRValue(name, IRIntegerType.get1());
        instruction.setName(name);
        IRBasicBlock block = new IRBasicBlock();
        block.addIrInstruction(instruction);
        this.blocks.add(block);
        if (pos) {
            // 正常
            if(relops.isEmpty()) {
                // 无relops,只有一个relExp
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Bne);
            } else if (relops.get(relops.size() - 1) == TokenType.EQL) {
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Beq);
            } else {
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Bne);
            }
        } else {
            // 取反
            if (relops.isEmpty()) {
                // 无relops,只有一个relExp
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Beq);
            } else if (relops.get(relops.size() - 1) == TokenType.EQL) {
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Bne);
            } else {
                irBr = new IRBr(left, ifLable,endLabel, IRInstructionType.Beq);
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
                instruction = new IRBinaryInstruction(IRIntegerType.get1(),IRInstructionType.Lt, left, right);
            } else if (addop == TokenType.LEQ) {
                instruction = new IRBinaryInstruction(IRIntegerType.get1(),IRInstructionType.Le, left, right);
            } else if (addop == TokenType.GRE) {
                instruction = new IRBinaryInstruction(IRIntegerType.get1(),IRInstructionType.Gt, left, right);
            } else if (addop == TokenType.GEQ) {
                instruction = new IRBinaryInstruction(IRIntegerType.get1(),IRInstructionType.Ge, left, right);
            } else {
                System.out.println("ERROR in generateIRInstructionFromRelExp!");
            }
            String name = "%LocalVar" + this.functionCnt.getCnt();
            left = new IRValue(name,IRIntegerType.get1());
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
            ParserTreeNode stmt;
            if (node .getType() == SyntaxType.BlockItem) {
                stmt = node.getFirstChild();
            } else {
                stmt = node;
            }
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
                } else if (tokenType == TokenType.SEMICN) {
                    // tokenType == TokenType.SEMICN
                    return BlockItemType.StmtSemicon;
                } else {
                    return BlockItemType.StmtOutput;
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
            } else {
                // if (stmt.getFirstChild().getType() == SyntaxType.Exp)
                return BlockItemType.StmtExp;
            }
        }
    }

    // Block
    private ArrayList<IRBasicBlock> generateIRBlockFromBlock() {
        int len = this.blockItems.size();
        int pointer = 0;
        while (pointer < len) {
            ParserTreeNode blockItem = this.blockItems.get(pointer);
            BlockItemType blockItemType = getItemType(blockItem);
            if (blockItemType.ordinal() <= BlockItemType.Block.ordinal()) {
                // 递归处理
                SymbolTable symbolTableSon = new SymbolTable(this.symbolTable);
                ParserTreeNode stmt;
                IRBlockBuilder blockBuilder;
                if (blockItemType == BlockItemType.StmtCond) {
                    stmt = blockItem.getFirstChild();
                    blockBuilder = new IRBlockBuilder(stmt, symbolTableSon, this.functionCnt, this.forBegin, this.forEnd);
                } else if (blockItemType == BlockItemType.StmtFor) {
                    stmt = blockItem.getFirstChild();
                    blockBuilder = new IRBlockBuilder(stmt, symbolTableSon, this.functionCnt, this.forBegin, this.forEnd);
                } else {
                    stmt = blockItem.getFirstChild().getFirstChild();
                    blockBuilder = new IRBlockBuilder(stmt, symbolTableSon, this.functionCnt, this.forBegin, this.forEnd, this.parentFunction);
                }
                ArrayList<IRBasicBlock> blocks = blockBuilder.generateIRBlocks();
                if (blocks != null) {
                    this.blocks.addAll(blocks);
                }
                pointer++;
            } else if (blockItemType.ordinal() < BlockItemType.StmtSemicon.ordinal()) {
                // 不需要递归处理
                IRBasicBlock block = new IRBasicBlock();
                block.setParentFunction(this.parentFunction);
                while (pointer < this.blockItems.size()) {
                    ParserTreeNode blockItemNow = this.blockItems.get(pointer);
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
            } else {
                // StmtSemicon
                // do nothing
                pointer++;
            }
        }
        return this.blocks;
    }
}
