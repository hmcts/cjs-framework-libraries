package uk.gov.justice.raml.jaxrs.maven;

import org.raml.model.Raml;
import uk.gov.justice.raml.core.Generator;
import uk.gov.justice.raml.core.GeneratorConfig;

/**
 * Generator for testing - the RAML and configuration are captured by the DummyGeneratorCaptor singleton class to be further used in unit tests.
 */
public class DummyGenerator implements Generator {

    @Override
    public void run(Raml raml, GeneratorConfig generatorConfig) {
        DummyGeneratorCaptor.getInstance().capture(raml, generatorConfig);
    }

}
