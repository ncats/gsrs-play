package ix.ginas.models.serialization;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import ix.ginas.models.v1.Amount;

/**
 * A BeanSerializerModifier we need to use to add
 * our custom {@link AmountSerializer} to a Mapper.
 *
 * Code should look like this:
 * <pre>
 * {@code
 *
 *  SimpleModule module = new SimpleModule();
 *  module.setSerializerModifier(new AmountSerializerModifier());
 *
 *  mapper.registerModule(module);
 *
 * }
 * </pre>
 */
public class AmountSerializerModifier extends BeanSerializerModifier {

    @Override
    public JsonSerializer<?> modifySerializer(
            SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        //We want to delegate amount serialization to the default jackson serailizer
        //unless it meets our new empty criteria.
        // This is a hack to prevent stackoverflow infinite looping
        //when Amount class gets added we grab the old default serailizer
        //and pass it to our custom serializer so it can delegate to the old seralizer
        //if the Amount should be serialized.
        if (beanDesc.getBeanClass().equals(Amount.class)) {
            return new AmountSerializer((JsonSerializer<Object>) serializer);
        }

        return serializer;
    }
}
