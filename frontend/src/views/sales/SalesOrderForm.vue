<template>
  <OrderFormPage
    title="销售出库"
    description="登记客户销售单，保存后自动扣减仓库库存"
    subject-label="客户"
    subject-placeholder="请选择客户"
    subject-key="customerId"
    price-field="salePrice"
    success-route-name="sales-orders"
    success-message="销售单保存成功"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :create-order="createSalesOrder"
  />
</template>

<script setup>
import { getCustomers } from '../../api/customer'
import { getProducts } from '../../api/product'
import { createSalesOrder } from '../../api/sales'
import OrderFormPage from '../business/components/OrderFormPage.vue'

function loadSubjects() {
  return getCustomers()
}
</script>
