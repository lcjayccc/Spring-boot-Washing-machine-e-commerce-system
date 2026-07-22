-- 商品条件查询字段与索引迁移（MySQL 8.0/8.4）
-- 执行前：备份数据库，并确认当前连接指向目标库。
-- 本脚本允许 Hibernate ddl-auto=update 已提前创建字段的情况，可重复执行。

SET @schema_name = DATABASE();

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE `product` ADD COLUMN `product_code` VARCHAR(64) NULL COMMENT ''商品编码''',
              'SELECT ''product.product_code already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'product'
      AND column_name = 'product_code'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @ddl = (
    SELECT IF(COUNT(*) = 0,
              'ALTER TABLE `product` ADD COLUMN `stock_location` VARCHAR(100) NULL COMMENT ''库存地点''',
              'SELECT ''product.stock_location already exists''')
    FROM information_schema.columns
    WHERE table_schema = @schema_name
      AND table_name = 'product'
      AND column_name = 'stock_location'
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

-- 历史商品使用主键生成稳定编码；执行唯一索引前如有冲突，ALTER TABLE 会失败并阻止错误约束落库。
UPDATE product
SET product_code = CONCAT(
        'WM-',
        LPAD(id, GREATEST(6, CHAR_LENGTH(CAST(id AS CHAR))), '0')
    )
WHERE product_code IS NULL OR TRIM(product_code) = '';

SET @unique_code_index_count = (
    SELECT COUNT(*)
    FROM (
        SELECT index_name
        FROM information_schema.statistics
        WHERE table_schema = @schema_name
          AND table_name = 'product'
          AND non_unique = 0
        GROUP BY index_name
        HAVING COUNT(*) = 1
           AND MAX(column_name = 'product_code') = 1
    ) AS unique_indexes
);
SET @ddl = IF(
    @unique_code_index_count = 0,
    'ALTER TABLE `product` ADD UNIQUE KEY `uk_product_product_code` (`product_code`)',
    'SELECT ''a unique product_code index already exists'''
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

SET @visibility_index_count = (
    SELECT COUNT(*)
    FROM (
        SELECT index_name
        FROM information_schema.statistics
        WHERE table_schema = @schema_name
          AND table_name = 'product'
        GROUP BY index_name
        HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index) = 'status,id'
    ) AS visibility_indexes
);
SET @ddl = IF(
    @visibility_index_count = 0,
    'CREATE INDEX `idx_product_visibility_id` ON `product` (`status`, `id`)',
    'SELECT ''a status,id index already exists'''
);
PREPARE statement FROM @ddl;
EXECUTE statement;
DEALLOCATE PREPARE statement;

-- 验证结果：两项都应返回 0 行/0。
SELECT product_code, COUNT(*) AS duplicate_count
FROM product
GROUP BY product_code
HAVING COUNT(*) > 1;

SELECT COUNT(*) AS blank_product_code_count
FROM product
WHERE product_code IS NULL OR TRIM(product_code) = '';

-- 历史数据稳定运行一段时间并确认所有写入口都提供编码后，再单独评估 NOT NULL 约束。
