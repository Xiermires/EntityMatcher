package org.entitymatcher;

import java.math.BigInteger;

class NumberConversion
{
    private static final int BYTE = 0;
    private static final int SHORT = 1;
    private static final int INT = 2;
    private static final int LONG = 3;
    private static final int FLOAT = 4;
    private static final int DOUBLE = 5;
    private static final int BIGINTEGER = 6;

    /**
     * Returns the numeric wrapper type ranked from [0 to 5] corresponding to the numeric wrapper sorted by min/max representable
     * numbers.
     * <p>
     * Returns {@link Double#NaN} if class is not a numeric wrapper.
     */
    public static int getRank(Class<?> wrapper)
    {
        if (Integer.class == wrapper || int.class == wrapper)
        {
            return INT;
        }
        else if (Double.class == wrapper || double.class == wrapper)
        {
            return DOUBLE;
        }
        else if (Float.class == wrapper || float.class == wrapper)
        {
            return FLOAT;
        }
        else if (Long.class == wrapper || long.class == wrapper)
        {
            return LONG;
        }
        else if (Short.class == wrapper || short.class == wrapper)
        {
            return SHORT;
        }
        else if (Byte.class == wrapper || byte.class == wrapper)
        {
            return BYTE;
        }
        else if (BigInteger.class == wrapper)
        {
            return BIGINTEGER;
        }
        return -1; // no convertible number.
    }

    public static Number convert(Object n, int rank)
    {
        if (n instanceof Number)
        {
            if (INT == rank)
            {
                return ((Number) n).intValue();
            }
            else if (DOUBLE == rank)
            {
                return ((Number) n).doubleValue();
            }
            else if (FLOAT == rank)
            {
                return ((Number) n).floatValue();
            }
            else if (LONG == rank)
            {
                return ((Number) n).longValue();
            }
            else if (SHORT == rank)
            {
                return ((Number) n).shortValue();
            }
            else if (BYTE == rank)
            {
                return ((Number) n).byteValue();
            }
            else if (BIGINTEGER == rank)
            {
                return new BigInteger(n.toString());
            }
        }
        return null;
    }
}

