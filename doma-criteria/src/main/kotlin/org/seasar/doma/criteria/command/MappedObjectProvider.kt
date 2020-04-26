package org.seasar.doma.criteria.command

import java.sql.ResultSet
import org.seasar.doma.criteria.declaration.Row
import org.seasar.doma.internal.jdbc.command.AbstractObjectProvider
import org.seasar.doma.jdbc.entity.EntityPropertyDesc
import org.seasar.doma.jdbc.entity.EntityPropertyType
import org.seasar.doma.jdbc.query.Query

class MappedObjectProvider<RESULT>(
    query: Query,
    propTypes: List<EntityPropertyType<*, *>>,
    private val mapper: (Row) -> RESULT
) : AbstractObjectProvider<RESULT>() {

    private val jdbcMappingVisitor = query.config.dialect.jdbcMappingVisitor

    private val indexMap = propTypes.mapIndexed { index, propType ->
        propType to index + 1
    }.toMap()

    override fun get(resultSet: ResultSet?): RESULT {
        val row = object : Row {
            override fun <CONTAINER> get(propType: EntityPropertyDesc<*, *, CONTAINER>): CONTAINER? {
                val index = indexMap[propType]
                require(index != null) {
                    "The propType '${propType.name}' is unknown. " +
                        "Ensure that you specified it to the select function."
                }
                val prop = propType.createProperty()
                fetch(resultSet, prop, index, jdbcMappingVisitor)
                @Suppress("UNCHECKED_CAST")
                return prop.get() as CONTAINER?
            }
        }
        return mapper(row)
    }
}
