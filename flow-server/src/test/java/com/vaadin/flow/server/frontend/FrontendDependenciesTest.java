package com.vaadin.flow.server.frontend;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vaadin.flow.server.frontend.ClassFinder.DefaultClassFinder;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Component0;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Component1;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Component2;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.FirstView;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.RootViewWithLayoutTheme;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.RootViewWithMultipleTheme;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.RootViewWithTheme;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.RootViewWithoutTheme;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.SecondView;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Theme1;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Theme2;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.Theme4;
import com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.ThirdView;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class FrontendDependenciesTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {

        // TODO: This is not working yet, need to be fixed and adjust the test //NOSONAR
        Field field = FieldUtils.getDeclaredField(FrontendDependencies.class, "LUMO", true);
        FieldUtils.removeFinalModifier(field, true);
        FieldUtils.writeStaticField(field, "com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents.ThemeDefault");
    }

    private FrontendDependencies create(Class<?> ...classes) throws Exception {
        FrontendDependencies frontendDependencies = new FrontendDependencies(new DefaultClassFinder(new HashSet<Class<?>>(new ArrayList<>(Arrays.asList(classes)))));
        return frontendDependencies;
    }

    @Test
    public void should_extractClassesFromSignatures() {
        Set<String> classes = new HashSet<>();
        FrontendClassVisitor visitor = new FrontendClassVisitor(null, null);

        visitor.addSignatureToClasses(classes,
                "(Lcom/vaadin/flow/component/tabs/Tabs;Ljava/lang/String;Ljava/lang/Character;CLjava/lang/Integer;ILjava/lang/Long;JLjava/lang/Double;DLjava/lang/Float;FLjava/lang/Byte;BLjava/lang/Boolean;Z)Lcom/vaadin/flow/component/button/Button;");
        assertEquals(11, classes.size());
        assertArrayEquals(new String [] {
                "",
                "java.lang.Float",
                "com.vaadin.flow.component.button.Button",
                "java.lang.Character",
                "java.lang.Long",
                "java.lang.Double",
                "java.lang.Boolean",
                "com.vaadin.flow.component.tabs.Tabs",
                "java.lang.String",
                "java.lang.Byte",
                "java.lang.Integer"}, classes.toArray());

        visitor.addSignatureToClasses(classes,
                "([Lcom/vaadin/flow/component/Component;)V");
        assertEquals(12, classes.size());
        assertTrue(classes.contains("com.vaadin.flow.component.Component"));

        visitor.addSignatureToClasses(classes,
                "(Lcom/vaadin/flow/component/orderedlayout/FlexComponent$Alignment;[Lcom/vaadin/flow/component/Component;)");
        assertEquals(13, classes.size());
        assertTrue(classes.contains("com.vaadin.flow.component.orderedlayout.FlexComponent$Alignment"));

        // Apart from proper signature representation, it should handle class names, and class paths
        visitor.addSignatureToClasses(classes, this.getClass().getName());
        assertTrue(classes.contains(this.getClass().getName()));

        visitor.addSignatureToClasses(classes, "com/vaadin/flow/server/frontend/FrontendDependenciesTestComponents$AnotherComponent");
        assertTrue(classes.contains("com.vaadin.flow.server.frontend.FrontendDependenciesTestComponents$AnotherComponent"));

    }

    @Test
    public void should_visitNpmPakageAnnotations() throws Exception {
        FrontendDependencies deps = create(Component1.class, Component2.class);
        assertEquals(2, deps.getPackages().size());
        assertTrue(deps.getPackages().containsKey("@vaadin/component-1"));
        assertTrue(deps.getPackages().containsKey("@vaadin/component-2"));
        assertEquals("1.1.1", deps.getPackages().get("@vaadin/component-1"));
        assertEquals("222.222.222", deps.getPackages().get("@vaadin/component-2"));
    }

    @Test
    public void when_MultipleVersions_should_returnFirstVisitedOne() throws Exception {
        FrontendDependencies deps = create(Component0.class);
        assertEquals("=2.1.0", deps.getPackages().get("@vaadin/component-0"));
    }

    @Test
    public void should_takeThemeFromView() throws Exception {
        FrontendDependencies deps = create(RootViewWithTheme.class);

        assertEquals(Theme4.class, deps.getThemeDefinition().getTheme());

        assertEquals(0, deps.getImports().size());

        assertEquals(1, deps.getModules().size());
        assertTrue(deps.getModules().contains("./theme-4.js"));

        assertEquals(0, deps.getPackages().size());

        assertEquals(1, deps.getScripts().size());
        assertTrue(deps.getScripts().contains("frontend://theme-0.js"));
    }

    @Test
    public void should_not_takeTheme_when_NoTheme() throws Exception {
        FrontendDependencies deps = create(RootViewWithoutTheme.class);

        assertNull(deps.getThemeDefinition());

        assertEquals(0, deps.getImports().size());
        assertEquals(2, deps.getModules().size());
        assertEquals(0, deps.getPackages().size());
        assertEquals(1, deps.getScripts().size());
    }

    @Test
    public void should_takeThemeFromLayout() throws Exception {
        FrontendDependencies deps = create(RootViewWithLayoutTheme.class);
        assertEquals(Theme1.class, deps.getThemeDefinition().getTheme());

        assertEquals(2, deps.getImports().size());
        assertEquals(8, deps.getModules().size());
        assertEquals(0, deps.getPackages().size());
        assertEquals(6, deps.getScripts().size());
    }


    @Test
    public void should_takeThemeFromView_when_MultipleTheme() throws Exception {
        FrontendDependencies deps = create(RootViewWithMultipleTheme.class);

        assertEquals(Theme2.class, deps.getThemeDefinition().getTheme());
        assertEquals("foo", deps.getThemeDefinition().getVariant());

        assertEquals(2, deps.getImports().size());
        assertEquals(4, deps.getModules().size());
        assertEquals(0, deps.getPackages().size());
        assertEquals(2, deps.getScripts().size());
    }

    @Test
    public void should_not_takeTheme_when_NoRootView() throws Exception {
        FrontendDependencies deps = create(SecondView.class);

        assertNull(deps.getThemeDefinition());

        assertEquals(1, deps.getImports().size());
        assertEquals(4, deps.getModules().size());
        assertEquals(0, deps.getPackages().size());
        assertEquals(2, deps.getScripts().size());
    }


    @Test
    public void should_summarize_when_MultipleViews() throws Exception {
        FrontendDependencies deps = create(SecondView.class, FirstView.class);

        assertEquals(Theme1.class, deps.getThemeDefinition().getTheme());

        assertEquals(2, deps.getImports().size());
        assertEquals(8, deps.getModules().size());
        assertEquals(1, deps.getPackages().size());
        assertEquals(6, deps.getScripts().size());
    }

    @Test
    public void should_resolveComponentFactories() throws Exception {
        FrontendDependencies deps = create(ThirdView.class);

         assertEquals(0, deps.getImports().size());
         assertEquals(3, deps.getModules().size());
         assertEquals(0, deps.getPackages().size());
         assertEquals(0, deps.getScripts().size());
         assertTrue(deps.getModules().contains("./my-component.js"));
         assertTrue(deps.getModules().contains("./my-static-factory.js"));
         assertTrue(deps.getModules().contains("./my-another-component.js"));
    }
}
