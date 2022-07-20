package io.nick.plugin.better.coding.utils;

import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class CodeTemplate {
    public static final CodeTemplate INSTANCE = new CodeTemplate();

    private final Configuration cfg;

    public CodeTemplate() {
        Version version = Configuration.VERSION_2_3_28;
        Configuration cfg = new Configuration(version);
        cfg.setClassLoaderForTemplateLoading(CodeTemplate.class.getClassLoader(), "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(true);
        cfg.setWrapUncheckedExceptions(true);
        cfg.setWhitespaceStripping(true);
        DefaultObjectWrapperBuilder objectWrapperBuilder = new DefaultObjectWrapperBuilder(version);
        objectWrapperBuilder.setExposeFields(true);
        cfg.setObjectWrapper(objectWrapperBuilder.build());
        this.cfg = cfg;
    }

    public String render(String templateName, Map<String, Object> params) {
        try {
            Template template = cfg.getTemplate(templateName);
            try (StringWriter writer = new StringWriter()) {
                template.process(params, writer);
                return writer.toString();
            }
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }
}
