package org.haplo.template.html;

public class RenderException extends Exception {
    private Driver driver;

    public RenderException(Driver driver, String message) {
        super(message);
        this.driver = driver;
    }

    public String getMessage() {
        Template template = driver.getLastTemplate();
        String templateName = (template == null) ? "(no template)" : template.getName();
        return "When rendering template '"+templateName+"': "+super.getMessage();
    }

    public Driver getDriver() {
        return this.driver;
    }
}
