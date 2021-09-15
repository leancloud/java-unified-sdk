package cn.leancloud.gson;

import com.google.gson.*;
import com.google.gson.internal.ConstructorConstructor;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class GeneralObjectAdapter<T> extends TypeAdapter<T> {
  private TypeAdapter<T> typeAdapter = null;

  public GeneralObjectAdapter(FieldNamingPolicy policy, TypeToken<T> typeToken) {
    ConstructorConstructor constructorConstructor = new ConstructorConstructor(new HashMap<Type, InstanceCreator<?>>());
    JsonAdapterAnnotationTypeAdapterFactory jsonAdapterFactory = new JsonAdapterAnnotationTypeAdapterFactory(constructorConstructor);
    Excluder excluder = Excluder.DEFAULT;
    ReflectiveTypeAdapterFactory reflectiveTypeAdapterFactory =
            new ReflectiveTypeAdapterFactory(constructorConstructor, policy, excluder, jsonAdapterFactory);
    typeAdapter = reflectiveTypeAdapterFactory.create(new GsonBuilder().setLenient().create(), typeToken);
  }

  public void write(JsonWriter writer, T object) throws IOException {
    this.typeAdapter.write(writer, object);
  }

  public T read(JsonReader reader) throws IOException {
    return this.typeAdapter.read(reader);
  }
}
