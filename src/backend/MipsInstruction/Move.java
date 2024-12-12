package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Move extends MipsInstruction {
    private final int target;
    private final int source;

    public Move(int target, int source) {
        super("move");
        this.target = target;
        this.source = source;
    }

    public Move(int target, int source, String comment) {
        this(target, source);
        this.setComment(comment);
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
        return this.getComment() +
                this.getInstName() + " " +
                RegisterName.getName(this.target) + ", " +
                RegisterName.getName(this.source) + "\n";
    }
}
