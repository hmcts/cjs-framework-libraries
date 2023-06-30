package uk.gov.justice.generation.pojo.integration.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.justice.generation.pojo.integration.utils.PojoGeneratorPropertiesBuilder.pojoGeneratorPropertiesBuilder;
import static uk.gov.justice.generation.pojo.plugin.classmodifying.AddHashcodeAndEqualsPlugin.newAddHashcodeAndEqualsPlugin;

import uk.gov.justice.generation.pojo.core.PojoGeneratorProperties;
import uk.gov.justice.generation.pojo.integration.utils.GeneratorUtil;
import uk.gov.justice.generation.pojo.integration.utils.OutputDirectories;
import uk.gov.justice.generation.pojo.plugin.classmodifying.AddAdditionalPropertiesToClassPlugin;
import uk.gov.justice.generation.pojo.plugin.typemodifying.SupportJavaOptionalsPlugin;
import uk.gov.justice.services.test.utils.core.files.ClasspathFileResource;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReferencePackageNameIT {

    private final GeneratorUtil generatorUtil = new GeneratorUtil();
    private final OutputDirectories outputDirectories = new OutputDirectories();

    private static final File JSON_SCHEMA_FILE = new ClasspathFileResource().getFileFromClasspath("/schemas/tests/reference-packege-name.json");

    @BeforeEach
    public void setup() throws Exception {
        outputDirectories.makeDirectories("./target/test-generation/tests/reference");
    }

    @Test
    public void shouldUseIdToConstructPackageNameAndClassName() throws Exception {

        final PojoGeneratorProperties generatorProperties = pojoGeneratorPropertiesBuilder()
                .withRootClassName("")
                .build();

        final List<Class<?>> newClasses = generatorUtil
                .withClassModifyingPlugin(new AddAdditionalPropertiesToClassPlugin())
                .withClassModifyingPlugin(newAddHashcodeAndEqualsPlugin())
                .withTypeModifyingPlugin(new SupportJavaOptionalsPlugin())
                .withGeneratorProperties(generatorProperties)
                .generateAndCompileJavaSource(
                        JSON_SCHEMA_FILE,
                        "uk.gov.justice.standards.events",
                        outputDirectories);

        assertThat(newClasses.size(), is(2));

        final Class<?> title = newClasses.get(0);
        final Class<?> person = newClasses.get(1);

        assertThat(person.getName(), is("uk.gov.justice.standards.events.Person"));
        assertThat(title.getName(), is("uk.gov.justice.references.Title"));
    }
}
