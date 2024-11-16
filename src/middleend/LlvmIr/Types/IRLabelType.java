package middleend.LlvmIr.Types;

public class IRLabelType extends IRValueType {
    // LabelType is a value type of [basic block]
    // 单例模式 singleton
    private static final IRLabelType labelType = new IRLabelType();

    private IRLabelType() {}

    public static IRLabelType getLabelType() {
        return labelType;
    }
}
