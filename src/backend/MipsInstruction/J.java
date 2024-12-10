package backend.MipsInstruction;

import java.util.ArrayList;

public class J extends MipsInstruction {
    private final String label; // 跳转目标地址

    public J(String label) {
        super("j");
        this.label = label;
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(this.getInstName() + " " + this.label + "\n");
        return ret;
    }
}
