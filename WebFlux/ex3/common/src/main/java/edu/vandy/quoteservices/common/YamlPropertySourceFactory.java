package edu.vandy.quoteservices.common;

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.util.Objects;
import java.util.Properties;

/**
 * This class implements the {@link PropertySourceFactory} interface
 * used to create a new {@link PropertySource} from a YAML file. It
 * takes in a name and an {@link EncodedResource} and returns a {@link
 * PropertiesPropertySource} object that can be used to retrieve
 * properties from the YAML file.
 */
public class YamlPropertySourceFactory
       implements PropertySourceFactory {
    /**
     * Create a new {@link PropertySource} object from the given
     * {@link EncodedResource}.
     *
     * @param name The name of the {@link PropertySource}
     * @param encodedResource the {@link EncodedResource} containing
     *        the YAML file
     * @return A new {@link PropertySource} object representing the
     *         properties in the YAML file
     */
    @Override
    public PropertySource<?> createPropertySource
        (String name,
         EncodedResource encodedResource) {
        YamlPropertiesFactoryBean factory = 
            new YamlPropertiesFactoryBean();
        factory.setResources(encodedResource.getResource());

        Properties properties = factory.getObject();

        return new PropertiesPropertySource
            (Objects.requireNonNull(encodedResource.getResource().getFilename()),
             Objects.requireNonNull(properties));
    }
}
