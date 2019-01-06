package com.riningan.retrofit2.converter.csv;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Converter;


public final class CsvResponseBodyConverter<T> implements Converter<ResponseBody, T> {
    private static final char BACKSLASH = '\\';
    private static final char COMMA = ',';
    private static final char QUOTATION = '"';


    private Gson mGson;
    private TypeAdapter<T> mAdapter;


    CsvResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        mGson = gson;
        mAdapter = adapter;
    }


    @Override
    public T convert(ResponseBody value) throws IOException {
        String[] rows = value.string().split("\n");
        value.close();
        if (rows.length < 2) {
            throw new IOException("Not valid csv: rows count is less than 2");
        }
        ArrayList<String> titles = getValues(rows[0]);
        if (titles.size() < 1) {
            throw new IOException("Not valid csv: columns count is less than 1");
        }
        JsonArray array = new JsonArray();
        for (int i = 1; i < rows.length; i++) {
            ArrayList<String> values = getValues(rows[i]);
            if (values.size() != titles.size()) {
                throw new IOException("Not valid csv: columns count in row " + i + " is not equals to columns count in header's row");
            }
            JsonObject object = new JsonObject();
            for (int j = 0; j < values.size(); j++) {
                try {
                    object.addProperty(titles.get(j), Long.parseLong(values.get(j)));
                } catch (NumberFormatException ignored1) {
                    try {
                        object.addProperty(titles.get(j), Double.parseDouble(values.get(j)));
                    } catch (NumberFormatException ignored2) {
                        if (values.get(j).equalsIgnoreCase("true") || values.get(j).equalsIgnoreCase("false")) {
                            object.addProperty(titles.get(j), Boolean.valueOf(values.get(j)));
                        } else {
                            object.addProperty(titles.get(j), values.get(j));
                        }
                    }
                }
            }
            array.add(object);
        }
        String json;
        if (array.size() == 1) {
            json = array.get(0).toString();
        } else {
            json = array.toString();
        }
        StringReader stringReader = new StringReader(json);
        JsonReader jsonReader = mGson.newJsonReader(stringReader);
        return mAdapter.read(jsonReader);
    }


    private ArrayList<String> getValues(String row) {
        row = row.trim();
        ArrayList<String> result = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean open = false;
        boolean backslash = false;
        for (int i = 0; i < row.length(); i++) {
            char cur = row.charAt(i);
            switch (cur) {
                case BACKSLASH:
                    backslash = true;
                    break;
                case QUOTATION:
                    if (backslash) {
                        value.append(cur);
                        backslash = false;
                    } else {
                        open = !open;
                    }
                    break;
                case COMMA:
                    if (backslash) {
                        value.append(BACKSLASH);
                        backslash = false;
                    }
                    if (open) {
                        value.append(cur);
                    } else {
                        result.add(value.toString());
                        value = new StringBuilder();
                    }
                    break;
                default:
                    if (backslash) {
                        value.append(BACKSLASH);
                        backslash = false;
                    }
                    value.append(cur);
                    break;
            }
        }
        if (backslash) {
            value.append(BACKSLASH);
        }
        result.add(value.toString());
        return result;
    }
}