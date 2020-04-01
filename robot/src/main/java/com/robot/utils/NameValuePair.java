package com.robot.utils;

/**
 * 键值对
 *
 * @author Laotang
 */
public class NameValuePair implements java.io.Serializable{

    // 字段名称
    private String name;

    // 操作符对象
    private Operator operator;

    // 字段值
    private Object value;


    public NameValuePair() {
    }

    public NameValuePair(String name, Object value) {
       this(name, value, Operator.EQ);
    }

    public NameValuePair(String name, Object value, Operator operator) {
        this.name = name;
        this.value = value;
        this.operator = operator;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Operator getOperator() {
        return operator;
    }

    public String getOperatorValue() {
        return null == operator ? "" : operator.getValue();
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public enum  Operator {

        EQ ("="),
        GT (">"),
        GTE (">="),
        LT ("<"),
        LTE ("<="),
        NE ("!="),
        IN ("in"),
        NIN ("not in"),
        WHERE ("where"),
        LIKE("like")

        ;

        private final String value;

        private Operator(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }
}
