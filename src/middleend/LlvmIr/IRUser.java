package middleend.LlvmIr;
import middleend.LlvmIr.Types.IRValueType;
import java.util.ArrayList;

public abstract class IRUser extends IRValue {
    private ArrayList<IRUse> uses;
    private int numOfOperands;

    public IRUser(IRValueType IRValueType) {
        super(IRValueType);
        this.uses = new ArrayList<>();
    }

    public IRUser(IRValueType IRValueType, int numOfOperands) {
        this(IRValueType);
        this.uses = new ArrayList<>();
        this.numOfOperands = numOfOperands;
    }

    public void addUse(IRUse use) {
        this.uses.add(use);
    }

    public int getNumOfOperands() {
        return numOfOperands;
    }

    public void setOperand(IRValue value, int index) {
        for (IRUse use : uses) {
            if (use.getOpPos() == index) {
                use.getValue().removeUse(use);
                use.setValue(value);
                value.addUse(use);
                return;
            }
        }
        IRUse newUse = new IRUse(value, this, index);
        this.uses.add(newUse);
    }

    public IRValue getOperand(int index) {
        for (IRUse use : this.uses) {
            if (use.getOpPos() == index) {
                return use.getValue();
            }
        }
        return null;
    }

    @Override
    public ArrayList<String> printIR() {
        return super.printIR();
    }
}
