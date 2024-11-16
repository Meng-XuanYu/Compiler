package middleend.LlvmIr.Types;

import middleend.LlvmIr.IRValue;

import java.util.ArrayList;

public class IRFunctionType extends IRValueType {
    private IRValueType returnType;
    private ArrayList<IRValueType> parameterTypes; // 函数形参类型列表
    private ArrayList<IRValue> parameters; // 函数形参列表

    public IRFunctionType(IRValueType returnType, ArrayList<IRValueType> parameterTypes) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public ArrayList<IRValue> getParameters() {
        return this.parameters;
    }

    public IRValueType getReturnType() {
        return returnType;
    }

    public ArrayList<IRValueType> getParameterTypes() {
        return parameterTypes;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        if (this.returnType instanceof IRIntegerType) {
            ans.add("i32");
        } else if (this.returnType instanceof IRCharType) {
            ans.add("i8");
        } else if (this.returnType instanceof IRVoidType) {
            ans.add("void");
        }
        return ans;
    }

    public void setParameters(ArrayList<IRValue> parameters) {
        this.parameters = parameters;
    }
}
