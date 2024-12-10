package backend.MipsFunction;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsModule;
import backend.MipsNode;
import backend.MipsSymbol.MipsSymbolTable;

import java.util.ArrayList;

public class MipsFunction implements MipsNode {
    private String name;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private MipsModule father;
    private MipsSymbolTable symbolTable;



}
