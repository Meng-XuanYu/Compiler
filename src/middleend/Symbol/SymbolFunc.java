package middleend.Symbol;

import java.util.ArrayList;

public class SymbolFunc extends Symbol {
    private boolean isReturn;
    private final ArrayList<Symbol> params;

    public SymbolFunc(String name, SymbolType type, int scope) {
        super(name, type, scope);
        this.isReturn = type == SymbolType.VoidFunc;
        this.params = new ArrayList<>();
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
            if (!areTypesCompatible(params.get(i).type(), paramTypes.get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean areTypesCompatible(SymbolType type1, SymbolType type2) {
        int type1Int, type2Int;
        type1Int = getTypeInt(type1);
        type2Int = getTypeInt(type2);
        if (type1Int == 2 && type2Int == 3) {
            return false;
        }
        if (type1Int == 3 && type2Int == 2) {
            return false;
        }
        if ((type1Int == 0 || type1Int == 1)&& (type2Int == 3 || type2Int == 2)) {
            return false;
        }
        return (type2Int != 0 && type2Int != 1) || (type1Int != 3 && type1Int != 2);
    }

    private int getTypeInt(SymbolType type) {
        int typeInt;
        if (type == SymbolType.Char || type == SymbolType.ConstChar || type == SymbolType.CharFunc) {
            typeInt = 0;
        } else if (type == SymbolType.Int || type == SymbolType.ConstInt || type == SymbolType.IntFunc) {
            typeInt = 1;
        } else if (type == SymbolType.CharArray || type == SymbolType.ConstCharArray) {
            typeInt = 2;
        } else if (type == SymbolType.IntArray || type == SymbolType.ConstIntArray) {
            typeInt = 3;
        } else {
            typeInt = 4;
        }
        return typeInt;
    }

    public void addParam(Symbol param) {
        params.add(param);
    }

    public boolean isReturn() {
        return isReturn;
    }
}
