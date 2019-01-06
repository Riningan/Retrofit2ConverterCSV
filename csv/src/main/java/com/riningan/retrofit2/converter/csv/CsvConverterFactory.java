package com.riningan.retrofit2.converter.csv;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;


public final class CsvConverterFactory extends Converter.Factory {
    public static CsvConverterFactory create() {
        return new CsvConverterFactory(new Gson());
    }

    public static CsvConverterFactory create(Gson gson) {
        if (gson == null) {
            throw new NullPointerException("gson == null");
        }
        return new CsvConverterFactory(gson);
    }


    private final Gson mGson;


    private CsvConverterFactory(Gson gson) {
        mGson = gson;
    }


    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type
            , Annotation[] parameterAnnotations
            , Annotation[] methodAnnotations
            , Retrofit retrofit) {
        TypeAdapter<?> adapter = mGson.getAdapter(TypeToken.get(type));
        return new CsvRequestBodyConverter<>(mGson, adapter);
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type
            , Annotation[] annotations
            , Retrofit retrofit) {
        TypeAdapter<?> adapter = mGson.getAdapter(TypeToken.get(type));
        return new CsvResponseBodyConverter<>(mGson, adapter);
    }
}