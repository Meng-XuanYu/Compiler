package middleend.LlvmIr.Value.BasicBlock;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.TokenType;
import middleend.LlvmIr.Value.Function.FunctionCnt;
import middleend.LlvmIr.Value.Instruction.IRLabel;
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
        if (node.getType() == SyntaxType.Block) {
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

        } else if (this.stmtFor != null) {

        } else {
            System.out.println("ERROR in IRBlockBuilder! No block or stmt!");
        }
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

    private BlockItemType getItemTyep(ParserTreeNode node) {
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
        for (int i = 0; i < this.blockItems.size(); i++) {
            ParserTreeNode blockItem = this.blockItems.get(i).getFirstChild();
            BlockItemType blockItemType = getItemTyep(blockItem);
            if (blockItemType.ordinal() < BlockItemType.Block.ordinal()) {
                // 递归处理
            } else {
                // 不需要递归处理

            }
        }
    }
}
