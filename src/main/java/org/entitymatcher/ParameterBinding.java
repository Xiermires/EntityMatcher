package org.entitymatcher;

import javax.persistence.Query;

public interface ParameterBinding
{
    /**
     * Binds the object as the next parameter of the query.
     */
    String bind(Object o);

    /**
     * Sets the bound parameters into the query.
     */
    String solveQuery(String rawQuery, Query query);
}
