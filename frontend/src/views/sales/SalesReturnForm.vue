<template>
  <OrderFormPage
    title="客户退货"
    description="登记客户退回商品，保存后自动增加仓库库存"
    subject-label="客户"
    subject-placeholder="请选择客户"
    subject-key="customerId"
    source-order-key="sourceOrderId"
    source-order-label="原销售单ID"
    remark-key="reason"
    remark-label="退货原因"
    price-field="salePrice"
    success-route-name="sales-returns"
    success-message="客户退货单保存成功"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :create-order="createSalesReturn"
  />
</template>

<script setup>
import { getCustomers } from '../../api/customer'
import { getProducts } from '../../api/product'
import { createSalesReturn } from '../../api/salesReturn'
import OrderFormPage from '../business/components/OrderFormPage.vue'

function loadSubjects() {
  return getCustomers()
}
</script>
