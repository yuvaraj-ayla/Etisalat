package com.aylanetworks.aylasdk.metrics;
/*
 * AylaSDK
 *
 * Copyright 2020 Ayla Networks, all rights reserved
 */
public class AylaFeatureMetric extends AylaMetric {

    public String error;
    public String result;

    /**
     * AylaFeatureMetricType enum
     */
    public enum AylaFeatureMetricType {
        /** Metric type for Rules */
        RULES("AylaFeatureMetricTypeRules"),
        /** Metric type for Notifications */
        NOTIFICATIONS("AylaFeatureMetricTypeNotifications"),
        /** Metric type for Schedules */
        SCHEDULES("AylaFeatureMetricTypeSchedules"),
        /** Metric type for Groups and Scenes */
        GROUPSANDSCENES("AylaFeatureMetricTypeGroupsAndScenes"),
        /** Metric type for Account creation */
        ACCOUNT_CREATION("AylaFeatureMetricTypeAccountCreation"),
        /** Metric type for Account confirmation */
        ACCOUNT_CONFIRMATION("AylaFeatureMetricTypeAccountConfimation");

        private String _stringValue;
        public String stringValue(){
            return _stringValue;
        }
        AylaFeatureMetricType(String val){
            _stringValue = val;
        }
    }

    /**
     * Constructor
     *
     * @param logLevel   Log level.
     * @param metricType Type of this metric.
     * @param methodName Method from which this metric is generated.
     */
    public AylaFeatureMetric(LogLevel logLevel, AylaFeatureMetricType metricType, String methodName) {
        super(logLevel, metricType.stringValue(), methodName);
    }
}
