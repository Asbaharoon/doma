/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.internal.jdbc.command;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.seasar.doma.MapKeyNamingType;
import org.seasar.doma.internal.jdbc.query.SelectQuery;
import org.seasar.doma.jdbc.IterationCallback;
import org.seasar.doma.jdbc.IterationContext;
import org.seasar.doma.jdbc.NoResultException;
import org.seasar.doma.jdbc.Sql;

/**
 * @author taedium
 * 
 */
public class MapIterationHandler<R> implements ResultSetHandler<R> {

    private final MapKeyNamingType keyNamingType;

    protected final IterationCallback<R, Map<String, Object>> iterationCallback;

    public MapIterationHandler(MapKeyNamingType keyNamingType,
            IterationCallback<R, Map<String, Object>> iterationCallback) {
        assertNotNull(keyNamingType, iterationCallback);
        this.keyNamingType = keyNamingType;
        this.iterationCallback = iterationCallback;
    }

    @Override
    public R handle(ResultSet resultSet, SelectQuery query) throws SQLException {
        assertNotNull(resultSet, query);
        MapFetcher fetcher = new MapFetcher(query, keyNamingType);
        IterationContext iterationContext = new IterationContext();
        boolean existent = false;
        R result = null;
        while (resultSet.next()) {
            existent = true;
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            fetcher.fetch(resultSet, map);
            result = iterationCallback.iterate(map, iterationContext);
            if (iterationContext.isExited()) {
                return result;
            }
        }
        if (query.isResultEnsured() && !existent) {
            Sql<?> sql = query.getSql();
            throw new NoResultException(sql);
        }
        return result;
    }

}
