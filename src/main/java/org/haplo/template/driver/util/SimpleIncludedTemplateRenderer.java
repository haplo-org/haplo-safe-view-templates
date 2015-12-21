package org.haplo.template.driver.util;

import java.util.Map;

import org.haplo.template.html.Driver;
import org.haplo.template.html.Template;
import org.haplo.template.html.Context;
import org.haplo.template.html.RenderException;

public class SimpleIncludedTemplateRenderer implements Driver.IncludedTemplateRenderer {
    private Map<String,Template> templates;

    public SimpleIncludedTemplateRenderer(Map<String,Template> templates) {
        this.templates = templates;
    }

    public void renderIncludedTemplate(String templateName, StringBuilder builder, Driver driver, Context context) throws RenderException {
        Template template = (this.templates == null) ? null : this.templates.get(templateName);
        if(template == null) {
            throw new RenderException(driver, "Could not find included template '"+templateName+"'");
        }
        template.renderAsIncludedTemplate(builder, driver, driver.getRootView(), context);
    }
}
