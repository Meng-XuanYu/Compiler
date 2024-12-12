package backend.MipsInstruction;

import java.util.ArrayList;

public class Jal extends MipsInstruction {
    private final String target;

    public Jal(String target) {
        super("jal");
        this.target = target;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                this.target + "\n";
        ArrayList<String> ans = new ArrayList<>();
        ans.add(sb);
        return ans;
    }
}
