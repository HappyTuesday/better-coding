package io.volantis.plugin.better.coding.utils;

import freemarker.template.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CodeTemplate {
    public static final CodeTemplate INSTANCE = new CodeTemplate();

    private static final String TEMPLATE_FOLDER = "templates";

    private final Configuration cfg;

    public CodeTemplate() {
        Version version = Configuration.VERSION_2_3_28;
        Configuration cfg = new Configuration(version);
        cfg.setClassLoaderForTemplateLoading(CodeTemplate.class.getClassLoader(), TEMPLATE_FOLDER);
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

    public String render(String templateName, String templateBody, Map<String, Object> params) {
        try {
            Template template = new Template(templateName, templateBody, cfg);
            try (StringWriter writer = new StringWriter()) {
                template.process(params, writer);
                return writer.toString();
            }
        } catch (IOException | TemplateException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getTemplate(String templateName) {
        try (var stream = CodeTemplate.class.getClassLoader().getResourceAsStream(TEMPLATE_FOLDER + "/" + templateName)) {
            if (stream == null) {
                return "";
            }
            return IOUtils.toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException e) {
            return "";
        }
    }
}
