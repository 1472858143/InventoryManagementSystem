<template>
  <OrderQueryPage
    title="采购退货查询"
    description="查询采购退货单、查看明细并处理作废冲销"
    subject-label="供应商"
    subject-field="supplierId"
    subject-param-key="supplierId"
    number-field="returnNo"
    source-field="sourceOrderId"
    remark-field="reason"
    remark-label="退货原因"
    create-route-name="purchase-return-create"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :list-orders="getPurchaseReturns"
    :get-detail="getPurchaseReturnDetail"
    :cancel-order="cancelPurchaseReturn"
  />
</template>

<script setup>
import { getProducts } from '../../api/product'
import { cancelPurchaseReturn, getPurchaseReturnDetail, getPurchaseReturns } from '../../api/purchaseReturn'
import { getSuppliers } from '../../api/supplier'
import OrderQueryPage from '../business/components/OrderQueryPage.vue'

function loadSubjects() {
  return getSuppliers()
}
</script>
