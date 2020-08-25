package ix.ginas.models.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ix.ginas.models.v1.Amount;

import java.io.IOException;

/**
 * A custom JSON Serializer for {@link Amount}s
 * to change what constitutes an "empty" Amount.
 * A Amount is considered non-empty
 * only if it has at least one field set in the
 * set of( average, low, lowlimit, high, highLimit,
 * nonNumericValue, units, or type).
 * If only fields like creator are set
 * then it is still considered empty.
 * This is because some external tools and UIs
 * that generate GSRS Substance JSON data
 * might prepopulate some amount fields
 * even if there is not a real amount included.
 */
public class AmountSerializer extends StdSerializer<Amount> {
    private final JsonSerializer<Object> defaultSerializer;

    public AmountSerializer(JsonSerializer<Object> defaultSerializer) {
        super(Amount.class);
        this.defaultSerializer = defaultSerializer;
    }

    @Override
    public void serialize(Amount value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

            defaultSerializer.serialize(value, gen, serializers);

    }
    @Override
    public boolean isEmpty(SerializerProvider provider, Amount value) {
        return value == null ||( value.average == null && value.low == null && value.lowLimit == null && value.high == null
                && value.highLimit == null && value.nonNumericValue == null && value.units == null && value.type == null);
    }


}
