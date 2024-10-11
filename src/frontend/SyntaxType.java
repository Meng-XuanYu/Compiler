package frontend;

public enum SyntaxType {
    CompUnit, // 编译单元
    Decl, // 声明
    ConstDecl, // 常量声明
    BType, // 基本类型
    ConstDef, // 常量定义
    ConstInitVal, // 常量初值
    VarDecl, // 变量声明
    VarDef, // 变量定义
    InitVal, // 变量初值
    FuncDef, // 函数定义
    MainFuncDef, // 主函数定义
    FuncType, // 函数类型
    FuncFParams, // 函数形参表
    FuncFParam, // 函数形参
    Block, // 语句块
    BlockItem, // 语句块项
    Stmt, // 语句
    ForStmt, // 语句 ForStmt
    Exp, // 表达式
    Cond, // 条件表达式
    LVal, // 左值表达式
    PrimaryExp, // 基本表达式
    Number, // 数值
    Character, // 字符
    UnaryExp, // 一元表达式
    UnaryOp, // 单目运算符
    FuncRParams, // 函数实参表
    MulExp, // 乘除模表达式
    AddExp, // 加减表达式
    RelExp, // 关系表达式
    EqExp, // 相等性表达式
    LAndExp, // 逻辑与表达式
    LOrExp, // 逻辑或表达式
    ConstExp, // 常量表达式
    Token // 给语法树用的
}