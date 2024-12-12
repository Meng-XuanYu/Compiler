package middleend.LlvmIr.Value.GlobalVar;
import middleend.LlvmIr.IRUser;
import middleend.LlvmIr.Types.IRIntArrayType;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Constant.IRConstant;
import middleend.LlvmIr.Value.Constant.IRConstantIntArray;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public class IRGlobalVar extends IRUser implements IRNode {
    private IRValueType type;
    private IRConstant initialValue;
    private boolean isConstant;
    private boolean isZero;

    // 名称和类型
    private IRGlobalVar(IRValueType type, String name) {
        super(type);
        this.type = type;
        this.setName(name);
    }

    // 名称、类型和初始值以及是否为常量
    public IRGlobalVar(IRValueType type, String name, IRConstant initialValue, boolean isConstant) {
        this(type, name);
        this.initialValue = initialValue;
        this.isConstant = isConstant;
        this.isZero = initialValue==null || initialValue instanceof IRConstantIntArray && ((IRConstantIntArray) initialValue).getConstantInts().isEmpty();
    }

    // 获取i32数组类型, i8数组类型的初始值
    public ArrayList<Integer> getIntInitArray() {
        IRConstantIntArray constantIntArray = (IRConstantIntArray) this.initialValue;
        int size = constantIntArray.getSize();
        ArrayList<Integer> ans = new ArrayList<>();
        for (int i = 0; i < size && i < constantIntArray.getConstantInts().size(); i++) {
            ans.add(Integer.parseInt(constantIntArray.getConstantInts().get(i).printIR().get(0)));
        }
        return ans;
    }

    public int getIntInit() {
        return Integer.parseInt(this.initialValue.printIR().getFirst());
    }

    public int getSize() {
        if (this.initialValue instanceof IRConstantIntArray) {
            return this.initialValue.getSize();
        }  else {
            return 0; // 代表不是数组
        }
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();

        if (this.isConstant) {
            String string;
            if (this.type.equals(IRIntegerType.get32())) {
                string = this.getName() + " = dso_local global " +
                        this.type.printIR().get(0) + " " + this.initialValue.printIR().get(0) + "\n";
            } else if (this.type.equals(IRIntegerType.get8())) {
                string = this.getName() + " = dso_local global " +
                        this.type.printIR().get(0) + " " + this.initialValue.printIR().get(0) + "\n";
            } else {
                // 数组类型
                int size = ((IRIntArrayType) this.type).getSize();
                boolean ischar = ((IRIntArrayType) this.type).ischar();
                if (ischar) {
                    if (isZero) {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i8" + "] zeroinitializer\n";
                    } else {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i8" + "] " + this.initialValue.printIR().get(0) + "\n";
                    }
                } else {
                    if (isZero) {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i32" + "] zeroinitializer\n";
                    } else {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i32" + "] " + this.initialValue.printIR().get(0) + "\n";
                    }
                }
            }
            ans.add(string);
        } else {
            String string;
            if (this.type.equals(IRIntegerType.get32())) {
                string = this.getName() + " = dso_local global " +
                        this.type.printIR().get(0) + " " + this.initialValue.printIR().get(0) + "\n";
            } else if (this.type.equals(IRIntegerType.get8())) {
                string = this.getName() + " = dso_local global " +
                        this.type.printIR().get(0) + " " + this.initialValue.printIR().get(0) + "\n";
            } else {
                // 数组类型
                int size = ((IRIntArrayType) this.type).getSize();
                boolean ischar = ((IRIntArrayType) this.type).ischar();
                if (ischar) {
                    if (isZero) {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i8" + "] zeroinitializer\n";
                    } else {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i8" + "] " + this.initialValue.printIR().get(0) + "\n";
                    }
                } else {
                    if (isZero) {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i32" + "] zeroinitializer\n";
                    } else {
                        string = this.getName() + " = dso_local global [" + size + " x " +
                                "i32" + "] " + this.initialValue.printIR().get(0) + "\n";
                    }
                }
            }
            ans.add(string);
        }

        return ans;
    }
}
