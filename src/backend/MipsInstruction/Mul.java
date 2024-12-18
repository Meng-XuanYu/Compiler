package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Mul extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Mul(int target, int left, int right) {
        super("mulu");
        this.target = target;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.toString();
        ArrayList<String> ans = new ArrayList<>();
        ans.add(sb);
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
