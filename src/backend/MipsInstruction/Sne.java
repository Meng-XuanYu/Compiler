package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Sne extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Sne(int target, int left, int right) {
        super("sne");
        this.target = target;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(this.target) + ", " +
                RegisterName.getName(this.left) + ", " +
                RegisterName.getName(this.right) + "\n";
        ArrayList<String> ans = new ArrayList<>();
        ans.add(sb);
        return ans;
    }
}
