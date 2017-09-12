package uk.gov.justice.generation.pojo.generators.plugin.typename;

import uk.gov.justice.generation.pojo.dom.Definition;

import com.squareup.javapoet.TypeName;

/**
 * Interface for all plugins that modify return types and parameters of generated classes. For
 * example these plugins can change the types of nullable properties in a class to
 * {@link java.util.Optional}, support for {@link java.util.UUID} as return types and constructor
 * parameters, or support for {@link java.time.ZonedDateTime} as return types and constructor
 * parameters
 *
 * {@link SupportJavaOptionalsPlugin}
 * {@link SupportUuidsPlugin}
 * {@link SupportZonedDateTimePlugin}
 */
public interface TypeModifyingPlugin {

    /**
     * Modifies the TypeName (used as constructor parameters and getter return types) of
     * generated classes in some way.
     *
     * @param typeName The type name to be modified
     * @param definition The FieldDefinition of the type to be modified
     *
     * @return The modified type name
     */
    TypeName modifyTypeName(final TypeName typeName, final Definition definition);
}
