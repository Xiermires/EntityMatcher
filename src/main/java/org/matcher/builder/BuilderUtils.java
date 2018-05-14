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
package org.matcher.builder;


public class BuilderUtils {

    public static String toAlias(String table) {
	return table == null ? null : table.toLowerCase();
    }

    public static String aliasPlusColumn(Class<?> referent, String property) {
	final String alias = toAlias(getTableName(referent));
	final String column = getColumnName(referent, property);
	return aliasPlusColumn(alias, column);
    }
    
    public static String aliasPlusColumn(String table, String column) {
	return table == null && column == null ? "" : column == null ? table : table.concat(".").concat(column);
    }

    public static String getTableName(Class<?> clazz) {
	return clazz != null ? clazz.getSimpleName() : null;
    }

    public static String getColumnName(Class<?> clazz, String fieldName) {
	try {
	    return clazz != null && fieldName != null ? clazz.getDeclaredField(fieldName).getName() : null;
	} catch (NoSuchFieldException | SecurityException e) {
	    throw new IllegalArgumentException(//
		    "Class '" + clazz.getSimpleName() + "' doesn't contain field named '" + fieldName + "'.");
	}
    }
}
