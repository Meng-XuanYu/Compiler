package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Addi extends MipsInstruction {
    private final int target;
    private final int source;
    private final int immediate;

    public Addi(int target, int source, int immediate) {
        super("addiu");
        this.target = target;
        this.source = source;
        this.immediate = immediate;
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(this.toString());
        return ans;
    }
    
    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(target) + ", " +
                RegisterName.getName(source) + ", " +
                immediate + "\n";
    }
}
