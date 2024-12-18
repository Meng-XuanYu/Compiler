package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Andi extends MipsInstruction {
    private final int destReg;
    private final int srcReg;
    private final int immediate;

    public Andi(int destReg, int srcReg, int immediate) {
        super("andi");
        this.destReg = destReg;
        this.srcReg = srcReg;
        this.immediate = immediate;
    }

    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(destReg) + ", " +
                RegisterName.getName(srcReg) + ", " +
                immediate + "\n";
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(this.toString());
        return ans;
    }
}