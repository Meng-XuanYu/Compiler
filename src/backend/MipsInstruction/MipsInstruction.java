package backend.MipsInstruction;
import backend.MipsNode;
import java.util.ArrayList;

public abstract class MipsInstruction implements MipsNode {
    private final String instName;
    private String comment;

    public MipsInstruction(String instName) {
        this.instName = instName;
        this.comment = "";
    }

    public String getInstName() {
        return instName;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public ArrayList<String> printMips() {
        return null;
    }
}
