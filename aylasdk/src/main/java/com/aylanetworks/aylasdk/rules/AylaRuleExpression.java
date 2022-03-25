package com.aylanetworks.aylasdk.rules;

public interface AylaRuleExpression {

    /**
     * Returns the key of the rule.
     */
    String key();

    /**
     * Returns the full expression of the rule.
     */
    String create();
}
