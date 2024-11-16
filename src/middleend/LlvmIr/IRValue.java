package middleend.LlvmIr;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public abstract class IRValue implements IRNode {
    private String name;
    private IRValueType type;
    private ArrayList<IRUse> uses;
    private int size;// 如果是数组，size表示数组大小；否则size为0

    public IRValue(IRValueType IRValueType) {
        this.name = "";
        this.type = IRValueType;
        this.uses = new ArrayList<>();
    }

    public IRValue(String name, IRValueType IRValueType) {
        this.name = name;
        this.type = IRValueType;
        this.uses = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public IRValueType getType() {
        return type;
    }

    public void addUse(IRUse use) {
        this.uses.add(use);
    }

    public void removeUse(IRUse use) {
        for (int i = 0; i < uses.size(); i++) {
            if (uses.get(i).equals(use)) {
                uses.remove(i);
                return;
            }
        }
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    @Override
    public ArrayList<String> printIR() {
        return new ArrayList<>();
    }
}
