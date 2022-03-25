package com.aylanetworks.aylasdk.ams.action.params;

import com.aylanetworks.aylasdk.AylaLog;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AylaActionParamsTypeAdapterFactory implements TypeAdapterFactory {

    public static final String LOG_TAG = "ActionParamsTypeAdapterFactory";

    private final Map<Class<?>, TypeAdapter<?>> classToDelegate = new LinkedHashMap<>();

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {

        Class<? super T> rawType = type.getRawType();
        if (rawType != AylaActionParameters.class) {
            return null;
        }

        AylaLog.d(LOG_TAG, "create action params type adapter");

        List<Class<?>> classList = new ArrayList<>();
        classList.add(AylaUrlActionParameters.class);
        classList.add(AylaEmailActionParameters.class);
        classList.add(AylaDatapointActionParameters.class);
        for (Class<?> classType : classList) {
            TypeToken<?> typeToken = TypeToken.get(classType);
            classToDelegate.put(classType, gson.getDelegateAdapter(this, typeToken));
        }

        return new TypeAdapter<T>() {
            @Override
            public void write(JsonWriter out, T value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }

                Class<?> srcType = value.getClass();
                TypeAdapter<T> delegate = (TypeAdapter<T>) classToDelegate.get(srcType);

                if (delegate == null) {
                    delegate = (TypeAdapter<T>) gson.getDelegateAdapter(
                            AylaActionParamsTypeAdapterFactory.this, TypeToken.get(srcType));
                    AylaLog.d(LOG_TAG, "Asked gson for delegate for " + srcType + ", got " +
                            delegate);
                }

                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + srcType.getName()
                            + "; did you forget to register a subtype?");
                }

                JsonObject jsonObject;
                try {
                    jsonObject = delegate.toJsonTree(value).getAsJsonObject();
                } catch (Exception e) {
                    AylaLog.e(LOG_TAG, "toJsonTree failed: " + e.getMessage());
                    throw new JsonParseException("cannot serialize" + srcType.getName() + "to Json Tree Exception:"
                            + e.toString());
                }

                JsonObject obj = new JsonObject();
                for (Map.Entry<String, JsonElement> e : jsonObject.entrySet()) {
                    obj.add(e.getKey(), e.getValue());
                }

                Streams.write(obj, out);
            }

            @Override
            public T read(JsonReader in) throws IOException {
                JsonElement jsonElement = Streams.parse(in);
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                Class classType = AylaActionParameters.class;
                if (jsonObject.has("datapoint")) {
                    classType = AylaDatapointActionParameters.class;
                } else if (jsonObject.has("endpoint")) {
                    classType = AylaUrlActionParameters.class;
                } else if (jsonObject.has("email_to")) {
                    classType = AylaEmailActionParameters.class;
                }

                TypeAdapter<T> delegate = (TypeAdapter<T>)classToDelegate.get(classType);
                if (delegate == null) {
                    delegate = (TypeAdapter <T>) gson.getDelegateAdapter(
                            AylaActionParamsTypeAdapterFactory.this, TypeToken.get(classType));
                    AylaLog.d(LOG_TAG, "Asked gson for delegate for " + classType + ", got " +
                            delegate);
                }

                if (delegate == null) {
                    throw new JsonParseException("cannot serialize " + classType.getName()
                            + "; did you forget to register a subtype?");
                } else {
                    classToDelegate.put(classType, delegate);
                }

                return delegate.fromJsonTree(jsonElement);
            }
        };
    }
}
