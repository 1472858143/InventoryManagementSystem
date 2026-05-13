<template>
  <OrderQueryPage
    title="销售单查询"
    description="查询销售出库单、查看明细并处理作废冲销"
    subject-label="客户"
    subject-field="customerId"
    subject-param-key="customerId"
    number-field="orderNo"
    create-route-name="sales-order-create"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :list-orders="getSalesOrders"
    :get-detail="getSalesOrderDetail"
    :cancel-order="cancelSalesOrder"
  />
</template>

<script setup>
import { getCustomers } from '../../api/customer'
import { getProducts } from '../../api/product'
import { cancelSalesOrder, getSalesOrderDetail, getSalesOrders } from '../../api/sales'
import OrderQueryPage from '../business/components/OrderQueryPage.vue'

function loadSubjects() {
  return getCustomers()
}
</script>
