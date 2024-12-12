package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Div extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Div(int target, int left, int right) {
        super("div");
        this.target = target;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> printMips() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getInstName()).append(" ");
        if (target != -1) {
            sb.append(RegisterName.getName(this.target)).append(", ");
        }
        sb.append(RegisterName.getName(this.left)).append(", ").append(RegisterName.getName(this.right)).append("\n");
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb.toString());
        return ret;
    }
}
