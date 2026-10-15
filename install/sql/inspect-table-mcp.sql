-- Pure SELECT catalog query for read-only MCP connections. No session settings, variables or DDL.
SELECT DB_NAME() AS database_name, N'__SCHEMA__' AS schema_name, N'__TABLE__' AS table_name,
       SYSUTCDATETIME() AS captured_at_utc
WHERE DB_NAME() = N'__DATABASE__' AND OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U') IS NOT NULL;
SELECT c.column_id, c.name AS column_name, t.name AS type_name, c.max_length,
       c.precision, c.scale, c.is_nullable, c.is_identity, c.is_computed,
       c.collation_name, dc.definition AS default_definition,
       ic.seed_value, ic.increment_value, cc.definition AS computed_definition
FROM sys.columns c JOIN sys.types t ON c.user_type_id = t.user_type_id
LEFT JOIN sys.default_constraints dc ON c.default_object_id = dc.object_id
LEFT JOIN sys.identity_columns ic ON c.object_id = ic.object_id AND c.column_id = ic.column_id
LEFT JOIN sys.computed_columns cc ON c.object_id = cc.object_id AND c.column_id = cc.column_id
WHERE c.object_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U') AND DB_NAME() = N'__DATABASE__'
ORDER BY c.column_id;
SELECT i.name AS index_name, i.is_primary_key, i.is_unique, i.filter_definition,
       ic.key_ordinal, ic.is_included_column, c.name AS column_name
FROM sys.indexes i
JOIN sys.index_columns ic ON i.object_id = ic.object_id AND i.index_id = ic.index_id
JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE i.object_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U') AND DB_NAME() = N'__DATABASE__'
ORDER BY i.index_id, ic.key_ordinal, ic.index_column_id;
SELECT fk.name AS fk_name, OBJECT_SCHEMA_NAME(fk.parent_object_id) AS child_schema,
       OBJECT_NAME(fk.parent_object_id) AS child_table, pc.name AS child_column,
       OBJECT_SCHEMA_NAME(fk.referenced_object_id) AS parent_schema,
       OBJECT_NAME(fk.referenced_object_id) AS parent_table, rc.name AS parent_column,
       fk.delete_referential_action_desc, fk.update_referential_action_desc
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fc ON fc.constraint_object_id = fk.object_id
JOIN sys.columns pc ON pc.object_id = fc.parent_object_id AND pc.column_id = fc.parent_column_id
JOIN sys.columns rc ON rc.object_id = fc.referenced_object_id AND rc.column_id = fc.referenced_column_id
WHERE (fk.parent_object_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U')
       OR fk.referenced_object_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U')) AND DB_NAME() = N'__DATABASE__'
ORDER BY fk.name, fc.constraint_column_id;
SELECT name AS constraint_name, definition, is_disabled, is_not_trusted
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U') AND DB_NAME() = N'__DATABASE__';
SELECT name AS trigger_name, is_disabled, is_instead_of_trigger
FROM sys.triggers
WHERE parent_id = OBJECT_ID(N'[__SCHEMA__].[__TABLE__]', N'U') AND DB_NAME() = N'__DATABASE__';
