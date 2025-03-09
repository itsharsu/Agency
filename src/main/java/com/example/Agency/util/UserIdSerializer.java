package com.example.Agency.util;

import com.example.Agency.model.User;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class UserIdSerializer extends JsonSerializer<User> {

    @Override
    public void serialize(User user, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (user == null) {
            gen.writeNull();
        } else {
            gen.writeString(user.getUserId());
        }
    }
}
