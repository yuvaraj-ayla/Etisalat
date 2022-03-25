package com.aylanetworks.aylasdk.rules;

class AylaLogicRuleExpression implements AylaRuleExpression {

    public static final AylaLogicRuleExpression AND = new AylaLogicRuleExpression("&&");
    public static final AylaLogicRuleExpression OR  = new AylaLogicRuleExpression("||");

    private final String key;

    public AylaLogicRuleExpression(String key) {
        this.key = key;
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public String create() {
        return null;
    }
}
