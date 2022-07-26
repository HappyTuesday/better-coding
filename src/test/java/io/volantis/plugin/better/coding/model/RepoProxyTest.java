package io.volantis.plugin.better.coding.model;

import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;

public class RepoProxyTest extends LightJavaCodeInsightFixtureTestCase {
    @Override
    protected @NotNull LightProjectDescriptor getProjectDescriptor() {
        return JAVA_8;
    }

    public void testRepoProxy() {
        
    }
}
