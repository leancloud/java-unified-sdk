package cn.leancloud.ops;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public abstract class NumericOperation extends BaseOperation {
  public NumericOperation(String op, String field, Object value) {
    super(op, field, value, false);
    if (getValue() instanceof Number) {
      // ok
    } else {
      throw new IllegalArgumentException("Value is not number!");
    }
  }

  public Object apply(Object obj) {
    if (null == obj) {
      return getValue();
    } else if (obj instanceof Number) {
      Number result = 0;
      if (this instanceof DecrementOperation) {
        result = subNumbers((Number) obj, (Number) this.value);
      } else if (this instanceof IncrementOperation) {
        result = addNumbers((Number) obj, (Number) this.value);
      } else if (this instanceof BitXorOperation) {
        result = calculateLongs((Number)obj, (Number)this.value, 'X');
      } else if (this instanceof BitAndOperation) {
        result = calculateLongs((Number)obj, (Number)this.value, 'A');
      } else if (this instanceof BitOrOperation) {
        result = calculateLongs((Number)obj, (Number)this.value, 'O');
      }
      return result;
    } else {
      LOGGER.w("cannot apply AddOperation on non number attribute. targetValueType=" + obj.getClass().getSimpleName());
    }
    return obj;
  }

  public Map<String, Object> encode() {
    Map<String, Object> opMap = new HashMap<String, Object>();
    opMap.put(BaseOperation.KEY_OP, this.getOperation());
    if (this instanceof IncrementOperation || this instanceof DecrementOperation) {
      // {"balance":{"__op":"Decrement","amount": 30}}
      // {"balance":{"__op":"Increment","amount": 30}}
      opMap.put(BaseOperation.KEY_AMOUNT, this.getValue());
    } else {
      // {"balance":{"__op":"BitAnd","value": 30}}
      opMap.put(BaseOperation.KEY_VALUE, this.getValue());
    }
    Map<String, Object> result = new HashMap<String, Object>();
    result.put(getField(), opMap);
    return result;
  }

  @Override
  protected ObjectFieldOperation mergeWithPrevious(ObjectFieldOperation other) {
    if (other instanceof SetOperation || other instanceof DeleteOperation) {
      return other;
    } else if (other instanceof NumericOperation) {
      return new CompoundOperation(this.field, other, this);
    } else if (other instanceof CompoundOperation) {
      return ((CompoundOperation) other).mergeWithPrevious(this);
    } else {
      reportIllegalOperations(this, other);
    }

    return NullOperation.INSTANCE;
  }

  protected static Long calculateLongs(Number a, Number b, char op) {
    Long first = a.longValue();
    Long second = b.longValue();
    Long result = 0l;
    switch (op) {
      case 'A':
        result = first & second;
        break;
      case 'O':
        result = first | second;
        break;
      case 'X':
        result = first ^ second;
        break;
      default:
        break;
    }
    return result;
  }

  protected static Number addNumbers(Number a, Number b) {
    if(a instanceof Double || b instanceof Double) {
      return a.doubleValue() + b.doubleValue();
    } else if(a instanceof Float || b instanceof Float) {
      return  a.floatValue() + b.floatValue();
    } else if(a instanceof Long || b instanceof Long) {
      return a.longValue() + b.longValue();
    } else {
      return a.intValue() + b.intValue();
    }
  }

  protected static Number subNumbers(Number a, Number b) {
    if(a instanceof Double || b instanceof Double) {
      return a.doubleValue() - b.doubleValue();
    } else if(a instanceof Float || b instanceof Float) {
      return a.floatValue() - b.floatValue();
    } else if(a instanceof Long || b instanceof Long) {
      return a.longValue() - b.longValue();
    } else {
      return a.intValue() - b.intValue();
    }
  }
}
