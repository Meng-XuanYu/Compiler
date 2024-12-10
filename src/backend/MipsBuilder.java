package backend;

import middleend.LlvmIr.IRModule;

public class MipsBuilder {
    private IRModule irModule;

    public MipsBuilder(IRModule irModule) {
        this.irModule = irModule;
    }

    public MipsModule genMipsModule() {
        MipsModule mipsModule = new MipsModule();
        return mipsModule;
    }
}
