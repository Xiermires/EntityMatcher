/*******************************************************************************
 * Copyright (c) 2017, Xavier Miret Andres <xavier.mires@gmail.com>
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
package org.entitymatcher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Query;

/**
 * This class binds jpql parameters for a single query and stores the parameters for later solving.
 */
class ParameterBindingImpl implements ParameterBinding
{
    private int suffix;
    final List<Object> l = new ArrayList<Object>();

    @Override
    public String bind(Object o)
    {
        if (o == null)
            return " IS NULL";

        l.add(o);
        return "?" + suffix++;
    }

    private final Pattern jpql = Pattern.compile("\\?(\\d+)");

    @Override
    public String solveQuery(String rawQuery, Query query)
    {
        final Matcher matcher = jpql.matcher(rawQuery);
        final StringBuffer sb = new StringBuffer();
        final Iterator<Object> it = l.iterator();
        int s = 0;
        while (matcher.find())
        {
            assert it.hasNext() : "bad param binding";
            final String key = matcher.group(1);
            final Object value = it.next();

            query.setParameter(Integer.valueOf(key), value);
        }
        sb.append(rawQuery.substring(s));
        return sb.toString();
    }

    @Override
    public String createParam(Object o)
    {
        if (o == null)
            return " IS NULL";

        l.add(o);
        return "?" + suffix++;
    }

    @Override
    public String resolveParams(String rawQuery, Query query)
    {
        final Matcher matcher = jpql.matcher(rawQuery);
        final StringBuffer sb = new StringBuffer();
        final Iterator<Object> it = l.iterator();
        int s = 0;
        while (matcher.find())
        {
            assert it.hasNext() : "bad param binding";
            final String key = matcher.group(1);
            final Object value = it.next();

            query.setParameter(Integer.valueOf(key), value);
        }
        sb.append(rawQuery.substring(s));
        return sb.toString();
    }
}
