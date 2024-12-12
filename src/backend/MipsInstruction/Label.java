package backend.MipsInstruction;

import java.util.ArrayList;

public class Label extends MipsInstruction {
    private String labelName;

    public Label(String labelName) {
        super(labelName);
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(this.getInstName() + ":\n");
        return ans;
    }
}
