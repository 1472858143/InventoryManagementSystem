<template>
  <OrderQueryPage
    title="客户退货查询"
    description="查询客户退货单、查看明细并处理作废冲销"
    subject-label="客户"
    subject-field="customerId"
    subject-param-key="customerId"
    number-field="returnNo"
    source-field="sourceOrderId"
    remark-field="reason"
    remark-label="退货原因"
    create-route-name="sales-return-create"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :list-orders="getSalesReturns"
    :get-detail="getSalesReturnDetail"
    :cancel-order="cancelSalesReturn"
  />
</template>

<script setup>
import { getCustomers } from '../../api/customer'
import { getProducts } from '../../api/product'
import { cancelSalesReturn, getSalesReturnDetail, getSalesReturns } from '../../api/salesReturn'
import OrderQueryPage from '../business/components/OrderQueryPage.vue'

function loadSubjects() {
  return getCustomers()
}
</script>
