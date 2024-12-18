package middleend.LlvmIr;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public class IRValue implements IRNode {
    private String name;
    private IRValueType type;
    private ArrayList<IRUse> uses;
    private int size;// 如果是数组，size表示数组大小；否则size为0
    private ArrayList<Integer> inits1;

    // 函数调用时的维数，-1表示常数，0表示一维，1表示二维
    // 此处架构设计借鉴
    private int dimensionValue = -1;
    private IRValue dimension1Value = null;
    // **dimension**表示数组的维度（例如标量、1维数组、2维数组）。
    // **dimensionValue**则决定了实际参数的维度，并根据维度的不同，生成不同的LLVM IR语法。

    private boolean isParam = false;

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

    // 构造函数专用
    public IRValue(String name, IRValueType IRValueType, boolean isParam) {
        this.name = name;
        this.type = IRValueType;
        this.uses = new ArrayList<>();
        this.isParam = isParam;
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

    public boolean isParam() {
        return isParam;
    }

    public void setParam(boolean isParam) {
        this.isParam = isParam;
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

    public void setValueType(IRIntegerType irIntegerType) {
        this.type = irIntegerType;
    }

    public void setDimensionValue(int dimensionValue) {
        this.dimensionValue = dimensionValue;
    }

    public int getDimensionValue() {
        return dimensionValue;
    }

    public void setDimension1Value(IRValue dimension1Value) {
        this.dimension1Value = dimension1Value;
    }

    public IRValue getDimension1Value() {
        return dimension1Value;
    }

    public void setInits1(ArrayList<Integer> inits1) {
        this.inits1 = inits1;
    }

    public ArrayList<Integer> getInits1() {
        return inits1;
    }

}
