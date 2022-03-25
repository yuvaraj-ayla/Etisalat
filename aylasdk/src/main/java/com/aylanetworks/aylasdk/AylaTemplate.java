package com.aylanetworks.aylasdk;

/**
 * Represents a generic message template with a template ID.
 */
public class AylaTemplate {

    private final String templateId;

    public AylaTemplate(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateId() {
        return templateId;
    }

}
