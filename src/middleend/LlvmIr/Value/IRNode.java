package middleend.LlvmIr.Value;
import java.util.ArrayList;

public interface IRNode {
    // 用于打印IR
    // 接口设计借鉴于往届
    ArrayList<String> printIR();
}
