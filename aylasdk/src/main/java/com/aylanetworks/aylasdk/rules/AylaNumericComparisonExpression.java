package com.aylanetworks.aylasdk.rules;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class AylaNumericComparisonExpression implements AylaRuleExpression {

    public static final AylaNumericComparisonExpression GT = new AylaNumericComparisonExpression(Comparator.GT);
    public static final AylaNumericComparisonExpression GE = new AylaNumericComparisonExpression(Comparator.GE);
    public static final AylaNumericComparisonExpression LT = new AylaNumericComparisonExpression(Comparator.LT);
    public static final AylaNumericComparisonExpression LE = new AylaNumericComparisonExpression(Comparator.LE);
    public static final AylaNumericComparisonExpression NE = new AylaNumericComparisonExpression(Comparator.NE);
    public static final AylaNumericComparisonExpression EQ = new AylaNumericComparisonExpression(Comparator.EQ);

    public static class Comparator {
        @StringDef({EQ, LE, GE, LT, GT, NE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface AllowedType {}

        public static final String EQ = "==";
        public static final String LE = "<=";
        public static final String GE = ">=";
        public static final String LT = "<";
        public static final String GT = ">";
        public static final String NE = "!=";
        public static final String CH = "#"; // change
        public static final String AN = "*"; // any
        public static final String CT = "?"; // str_contains
    }

    private final String key;

    AylaNumericComparisonExpression(@Comparator.AllowedType String key) {
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
