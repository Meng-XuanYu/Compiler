package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Sll extends MipsInstruction {
    private final int target;
    private final int source;
    private final int offset;
    
    public Sll(int target, int source, int offset) {
        super("sll");
        this.target = target;
        this.source = source;
        this.offset = offset;
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
                RegisterName.getName(this.target) + ", " +
                RegisterName.getName(this.source) + ", " +
                offset + "\n";
    }
}
