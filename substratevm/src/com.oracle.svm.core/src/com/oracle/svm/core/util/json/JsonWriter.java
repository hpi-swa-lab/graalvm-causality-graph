/*
 * Copyright (c) 2019, 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.util.json;

import org.graalvm.collections.EconomicMap;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.graalvm.collections.EconomicMap;

public class JsonWriter implements AutoCloseable {
    private final Writer writer;

    private int indentation = 0;

    public JsonWriter(Path path, OpenOption... options) throws IOException {
        this(Files.newBufferedWriter(path, StandardCharsets.UTF_8, options));
    }

    public JsonWriter(Writer writer) {
        this.writer = writer;
    }

    public JsonWriter append(char c) throws IOException {
        writer.write(c);
        return this;
    }

    public JsonWriter append(String s) throws IOException {
        writer.write(s);
        return this;
    }

    public JsonWriter appendObjectStart() throws IOException {
        return append('{');
    }

    public JsonWriter appendObjectEnd() throws IOException {
        return append('}');
    }

    public JsonWriter appendArrayStart() throws IOException {
        return append('[');
    }

    public JsonWriter appendArrayEnd() throws IOException {
        return append(']');
    }

    public JsonWriter appendSeparator() throws IOException {
        return append(',');
    }

    public JsonWriter appendFieldSeparator() throws IOException {
        return append(':');
    }

    public JsonWriter appendKeyValue(String key, Object value) throws IOException {
        return quote(key).appendFieldSeparator().quote(value);
    }

    @SuppressWarnings("unchecked")
    public void print(Map<String, Object> map) throws IOException {
        if (map.isEmpty()) {
            append("{}");
            return;
        }
        append('{');
        Iterator<String> keySetIter = map.keySet().iterator();
        while (keySetIter.hasNext()) {
            String key = keySetIter.next();
            Object value = map.get(key);
            quote(key).append(':');
            print(value);
            if (keySetIter.hasNext()) {
                append(',');
            }
        }
        append('}');
    }

    public void print(EconomicMap<String, Object> map) throws IOException {
        if (map.isEmpty()) {
            append("{}");
            return;
        }
        append('{');
        Iterator<String> keySetIter = map.getKeys().iterator();
        while (keySetIter.hasNext()) {
            String key = keySetIter.next();
            Object value = map.get(key);
            quote(key).append(':');
            print(value);
            if (keySetIter.hasNext()) {
                append(',');
            }
        }
        append('}');
    }

    public void print(Object[] array) throws IOException {
        append('[');
        for(int i = 0; i < array.length; i++) {
            Object e = array[i];
            print(e);
            if(i < array.length - 1)
                append(',');
        }
        append(']');
    }

    @SuppressWarnings("unchecked")
    public void print(Object value) throws IOException {
        if (value instanceof Map) {
            print((Map<String, Object>) value);
        } else if (value instanceof EconomicMap) {
            print((EconomicMap<String, Object>) value);
        } else if (value instanceof Object[]) {
            print((Object[]) value);
        } else {
            quote(value);
        }
    }

    public void print(List<String> list) throws IOException {
        print(list, s -> s);
    }

    public <T> void print(List<T> list, Function<T, String> mapper) throws IOException {
        if (list.isEmpty()) {
            append("[]");
            return;
        }
        append('[');
        Iterator<T> iter = list.iterator();
        while (iter.hasNext()) {
            quote(mapper.apply(iter.next()));
            if (iter.hasNext()) {
                append(',');
            }
        }
        append(']');
    }

    public JsonWriter quote(Object o) throws IOException {
        if (o == null) {
            return append("null");
        } else if (Boolean.TRUE.equals(o)) {
            return append("true");
        } else if (Boolean.FALSE.equals(o)) {
            return append("false");
        } else if (o instanceof Number) {
            return append(o.toString());
        } else {
            return quote(o.toString());
        }
    }

    public JsonWriter quote(String s) throws IOException {
        writer.write(quoteString(s));
        return this;
    }

    public static String quoteString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(2 + s.length() + 8 /* room for escaping */);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') {
                sb.append('\\');
                sb.append(c);
            } else if (c < 0x001F) {
                sb.append(String.format("\\u%04x", (int) c));
            } else {
                sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public JsonWriter newline() throws IOException {
        StringBuilder builder = new StringBuilder(1 + 2 * indentation);
        builder.append("\n");
        for (int i = 0; i < indentation; ++i) {
            builder.append("  ");
        }
        writer.write(builder.toString());
        return this;
    }

    public JsonWriter indent() {
        indentation++;
        return this;
    }

    public JsonWriter unindent() {
        assert indentation > 0;
        indentation--;
        return this;
    }

    public void flush() throws IOException {
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }
}
