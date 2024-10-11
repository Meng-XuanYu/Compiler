package frontend;

import java.util.ArrayList;

public class SymbolFunc extends Symbol {
    private boolean isReturn;
    private ArrayList<Symbol> params;

    public SymbolFunc(String name, SymbolType type, int scope) {
        super(name, type, scope);
        this.isReturn = type == SymbolType.VoidFunc;
        this.params = null;
    }

    public int getParamCount() {
        return params == null ? 0 : params.size();
    }

    public void setReturn() {
        this.isReturn = true;
    }

    public boolean paramCorrect(ArrayList<SymbolType> paramTypes) {
        if (params == null) {
            return paramTypes.isEmpty();
        }
        if (params.size() != paramTypes.size()) {
            return false;
        }
        for (int i = 0; i < params.size(); i++) {
            if (!params.get(i).type().equals(paramTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    public void setParams(ArrayList<Symbol> params) {
        this.params = params;
    }
}
