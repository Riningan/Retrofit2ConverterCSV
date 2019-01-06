package com.riningan.retrofit2.converter.csv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;


public class CsvRequestBodyConverter<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType.parse("text/csv; charset=UTF-8");
    private static final Charset UTF_8 = Charset.forName("UTF-8");


    private Gson mGson;
    private TypeAdapter<T> mAdapter;


    CsvRequestBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        mGson = gson;
        mAdapter = adapter;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public RequestBody convert(T value) throws IOException {
        Buffer buffer = new Buffer();
        Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
        JsonWriter jsonWriter = mGson.newJsonWriter(writer);
        mAdapter.write(jsonWriter, value);
        jsonWriter.close();
        JsonElement json = mGson.fromJson(buffer.readUtf8(), JsonElement.class);
        ArrayList<Map<String, String>> valuesList = getValuesList(json);
        // check values is equals
        for (int i = 1; i < valuesList.size(); i++) {
            Map<String, String> previousValues = valuesList.get(i - 1);
            Map<String, String> currentValues = valuesList.get(i);
            if (!previousValues.keySet().equals(currentValues.keySet())) {
                throw new IOException("Not equals elements: " + String.valueOf(i - 1) + " and " + i);
            }
        }
        // create csv
        StringBuilder csv = new StringBuilder();
        ArrayList<String> titles = new ArrayList<>();
        for (Map.Entry<String, String> entry : valuesList.get(0).entrySet()) {
            if (csv.length() > 0) {
                csv.append(",");
            }
            titles.add(entry.getKey());
            csv.append(entry.getKey());
        }
        csv.append("\n");
        for (Map<String, String> values : valuesList) {
            csv.append(values.get(titles.get(0)));
            for (int i = 1; i < titles.size(); i++) {
                csv.append(",").append(values.get(titles.get(i)));
            }
            csv.append("\n");
        }
        return RequestBody.create(MEDIA_TYPE, csv.toString().trim());
    }


    private ArrayList<Map<String, String>> getValuesList(JsonElement json) throws IOException {
        ArrayList<Map<String, String>> valuesList = new ArrayList<>();
        if (json.isJsonArray()) {
            JsonArray jsonArray = (JsonArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement jsonElement = jsonArray.get(i);
                if (jsonElement.isJsonObject()) {
                    valuesList.add(getValues((JsonObject) jsonElement));
                } else {
                    throw new IOException("Not valid json array element: " + jsonElement.toString());
                }
            }
            if (valuesList.size() == 0) {
                throw new IOException("Not valid json array: " + json.toString());
            }
        } else if (json.isJsonObject()) {
            valuesList.add(getValues((JsonObject) json));
        } else {
            throw new IOException("Not valid json type: " + json.toString());
        }
        return valuesList;
    }

    private Map<String, String> getValues(JsonObject jsonObject) throws IOException {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            if (entry.getValue().isJsonPrimitive()) {
                JsonPrimitive jsonPrimitive = (JsonPrimitive) entry.getValue();
                if (jsonPrimitive.isNumber()) {
                    values.put(entry.getKey(), entry.getValue().getAsNumber().toString());
                } else if (jsonPrimitive.isString()) {
                    values.put(entry.getKey(), "\"" + entry.getValue().getAsString() + "\"");
                } else if (jsonPrimitive.isBoolean()) {
                    values.put(entry.getKey(), String.valueOf(entry.getValue().getAsBoolean()));
                } else {
                    throw new IOException("Not valid json object primitive value: " + entry.getValue().toString());
                }
            } else {
                throw new IOException("Not valid json object value: " + entry.getValue().toString());
            }
        }
        if (values.size() == 0) {
            throw new IOException("Not valid json to convert csv: " + jsonObject.toString());
        }
        return values;
    }
}