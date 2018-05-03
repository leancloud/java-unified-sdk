package cn.leancloud.ops;

public class OperationBuilder {
  public static enum OperationType {
    Set,            // 增加属性
    Delete,         // 删除属性
    Add,            // 在数组末尾添加对象
    AddUnique,      //在数组末尾添加不会重复的对象
    Remove,         // 从数组中删除对象
    AddRelation,    // 添加一个关系
    RemoveRelation, // 删除一个关系
    Increment,      // 递增
    Decrement,      // 递减
    BitAnd,         // 与运算
    BitOr,          // 或运算
    BitXor          // 异或运算
  }
  public static final OperationBuilder BUILDER = new OperationBuilder();
  private OperationBuilder() {

  }
  public ObjectFieldOperation create(OperationType type, String field, Object value) {
    ObjectFieldOperation operation = null;
    switch (type) {
      case Add:
        operation = new AddOperation(field, value);
        break;
      case Set:
        operation = new SetOperation(field, value);
        break;
      case Delete:
        operation = new DeleteOperation(field);
        break;
      case AddRelation:
        operation = new AddRelationOperation(field, value);
        break;
      case AddUnique:
        operation = new AddUniqueOperation(field, value);
        break;
      case Remove:
        operation = new RemoveOperation(field, value);
        break;
      case RemoveRelation:
        operation = new RemoveRelationOperation(field, value);
        break;
      case Increment:
        operation = new IncrementOperation(field, value);
        break;
      case Decrement:
        operation = new DecrementOperation(field, value);
        break;
      case BitAnd:
        operation = new BitAndOperation(field, value);
        break;
      case BitOr:
        operation = new BitOrOperation(field, value);
        break;
      case BitXor:
        operation = new BitXorOperation(field, value);
        break;
        default:
          operation = new NullOperation(field, value);
          break;
    }
    return operation;
  }
}
