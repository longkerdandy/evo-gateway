package com.github.longkerdandy.evo.adapter.hue.constant;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.NameBasedGenerator;
import org.junit.Test;

/**
 * Description Test
 */
public class DescriptionTest {

    @Test
    public void idTest() {
        NameBasedGenerator generator = Generators.nameBasedGenerator(NameBasedGenerator.NAMESPACE_URL);
        String descId = generator.generate("http://philips.com/hue?version=" + Description.VERSION).toString();
        assert descId.equals(Description.ID);
    }
}
