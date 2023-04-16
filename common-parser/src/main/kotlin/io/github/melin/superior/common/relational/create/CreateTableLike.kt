package io.github.melin.superior.common.relational.create

import io.github.melin.superior.common.PrivilegeType
import io.github.melin.superior.common.SqlType
import io.github.melin.superior.common.relational.Statement
import io.github.melin.superior.common.relational.TableId

data class CreateTableLike(
    val oldTableId: TableId,
    override val tableId: TableId,
    var ifNotExists: Boolean = false,
    var external: Boolean = false,
    var temporary: Boolean = false
) : Statement() {
    override val privilegeType: PrivilegeType = PrivilegeType.CREATE
    override val sqlType: SqlType = SqlType.DDL
}