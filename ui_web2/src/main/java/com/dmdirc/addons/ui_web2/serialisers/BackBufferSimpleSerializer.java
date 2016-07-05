package com.dmdirc.addons.ui_web2.serialisers;

import com.dmdirc.ui.messages.BackBuffer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * Serializes a {@link BackBuffer} without including the full content.
 */
public class BackBufferSimpleSerializer implements JsonSerializer<BackBuffer> {

    @Override
    public JsonElement serialize(final BackBuffer src, final Type typeOfSrc, final JsonSerializationContext context) {
        final JsonObject res = new JsonObject();
        res.addProperty("lines", src.getDocument().getNumLines());
        return res;
    }

}
