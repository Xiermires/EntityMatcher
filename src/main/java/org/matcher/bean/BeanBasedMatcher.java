/*******************************************************************************
 * Copyright (c) 2018, Xavier Miret Andres <xavier.mires@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package org.matcher.bean;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.matcher.bean.InvokationCapturer.Capture;

public class BeanBasedMatcher {
    /**
     * Returns an instance of a class which might be used to match {@link Statements}.
     */
    public static <T> T matcher(Class<T> clazz) {
	return InvokationCapturer.capturer(clazz, m -> isBeanGetter(m));
    }

    public static Capture getLastCapture() {
	return InvokationCapturer.getLastCapture();
    }

    static final Pattern isGetter = Pattern.compile("(get|is)(.+)");

    private static boolean isBeanGetter(Method m) {
	return isGetter.matcher(m.getName()).matches();
    }

    static String toAlias(String table) {
	return table == null ? null : table.toLowerCase();
    }

    static String toTable(String alias) {
	return alias == null ? null : alias.substring(0, 1).toUpperCase().concat(alias.substring(1));
    }

    static String camelUp(String s) {
	return s.substring(0, 1).toUpperCase().concat(s.substring(1));
    }

    static String camelDown(String s) {
	return s.substring(0, 1).toLowerCase().concat(s.substring(1));
    }

    static String tableColumn(String table, String column) {
	return table == null && column == null ? null : table.concat(".").concat(column);
    }

    static Class<?> getReferent(Capture c) {
	return c == null ? null : c.method.getDeclaringClass();
    }

    static String getPropertyName(Capture c) {
	return getPropertyName(getProperty(c));
    }

    static String getPropertyName(Field f) {
	return f.getName();
    }

    static Field getProperty(Capture c) {
	return c == null ? null : getProperty(c.method);
    }

    static Field getProperty(Method m) {
	final Matcher matcher = isGetter.matcher(m.getName());
	if (matcher.matches()) {
	    final String fieldName = camelDown(matcher.group(2));
	    return getField(m.getDeclaringClass(), fieldName);
	}
	throw new IllegalArgumentException("Not a getter '" + m.getName() + "'");
    }

    static Field getField(Class<?> type, String name) {
	try {
	    return type.getDeclaredField(name);
	} catch (NoSuchFieldException | SecurityException e) {
	    throw new IllegalArgumentException(type.getName() + "doesn't follow the java beans convention for field: "
		    + name);
	}
    }
}
