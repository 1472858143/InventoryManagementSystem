-- 数据库变更草案
-- 任务编号：IT-004
-- 创建日期：2026-05-13
-- 变更目的：业务升级第一阶段：业务模型与数据库设计
-- 说明：
-- 1. 本文件只记录当前任务的数据库设计草案。
-- 2. 不得直接覆盖正式 SQL。
-- 3. 经用户验收后，再决定是否同步到 sql/market.sql 和 Document/3-项目开发/market.sql。

-- 第一阶段待设计对象：
-- supplier：供应商主数据。
-- customer：客户主数据。
-- purchase_order / purchase_order_item：进货单据及明细，或评估是否扩展 inbound_order。
-- purchase_return_order / purchase_return_order_item：采购退货单据及明细。
-- sales_order / sales_order_item：销售单据及明细，或评估是否扩展 outbound_order。
-- sales_return_order / sales_return_order_item：客户退货单据及明细。
-- stock_adjustment_order：报损、报溢等库存调整单。
-- system_log：系统操作日志。
-- permission / role_permission：如需要完整角色权限，则新增权限表。

-- 库存日志建议补充字段：
-- source_type：来源单据类型，如 PURCHASE_IN、PURCHASE_RETURN_OUT、SALE_OUT、SALE_RETURN_IN、STOCK_LOSS、STOCK_OVERFLOW、STOCK_CHECK。
-- source_id：来源单据 ID。
-- operator：操作人。
-- reason：变更原因。

-- 当前阶段不写入正式 DDL，待业务模型确认后补充具体 SQL 草案。
