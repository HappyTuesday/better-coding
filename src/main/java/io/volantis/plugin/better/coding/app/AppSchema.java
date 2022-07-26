package io.volantis.plugin.better.coding.app;

import com.intellij.psi.PsiDirectory;
import io.volantis.plugin.better.coding.utils.CodingUtils;
import io.volantis.plugin.better.coding.proxy.ConverterProxy;
import io.volantis.plugin.better.coding.proxy.CopierProxy;
import io.volantis.plugin.better.coding.proxy.DtoProxy;
import io.volantis.plugin.better.coding.proxy.FetcherProxy;

public class AppSchema {
    public static final String CONVERTERS_FOLDER_NAME = "converters";
    public static final String FETCHERS_FOLDER_NAME = "fetchers";
    public static final String COPIERS_FOLDER_NAME = "copiers";

    protected final PsiDirectory directory;

    public AppSchema(PsiDirectory directory) {
        this.directory = directory;
    }

    public PsiDirectory getDirectory() {
        return directory;
    }

    public ConverterProxy getConverterProxy(DtoProxy dtoProxy) {
        return ConverterProxy.forDTO(dtoProxy, CodingUtils.getOrCreateSubdirectory(directory, CONVERTERS_FOLDER_NAME));
    }

    public FetcherProxy getFetcherProxy(DtoProxy dtoProxy) {
        return FetcherProxy.forDTO(dtoProxy, CodingUtils.getOrCreateSubdirectory(directory, FETCHERS_FOLDER_NAME));
    }

    public CopierProxy getCopierProxy(DtoProxy dtoProxy) {
        return CopierProxy.forDTO(dtoProxy, CodingUtils.getOrCreateSubdirectory(directory, COPIERS_FOLDER_NAME));
    }
}
