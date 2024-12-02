package middleend.LlvmIr.Value.Instruction;

public enum IRInstructionType {
    // Binary operations
    Add,
    Sub,
    Mul,
    Div,
    Mod,
    // Logical operations
    Lt, // <
    Le, // <=
    Ge, // >=
    Gt, // >
    Eq, // ==
    Ne, // !=
    Or,
    Not,
    // Branch operations
    Beq, // Branch if ==
    Bne, // Branch if !=
    Blt, // Branch if <
    Ble, // Branch if <=
    Bgt, // Branch if >
    Bge, // Branch if >=
    Goto, // Unconditional branch
    // Terminators
    Br,
    Call,
    Ret,
    // Memory operations
    Alloca,
    Load,
    Store,
    GEP, // Get Element Ptr
    Zext,
    Trunc,
    Phi,
    GetElementPtr,
    // Label
    Label,
}
