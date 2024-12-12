package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Add extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Add(int target, int left, int right) {
        super("addu");
        this.target = target;
        this.left = left;
        this.right = right;
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
                RegisterName.getName(this.left) + ", " +
                RegisterName.getName(this.right) + "\n";
    }
}
