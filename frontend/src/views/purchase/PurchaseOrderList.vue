<template>
  <OrderQueryPage
    title="进货单查询"
    description="查询进货入库单、查看明细并处理作废冲销"
    subject-label="供应商"
    subject-field="supplierId"
    subject-param-key="supplierId"
    number-field="orderNo"
    create-route-name="purchase-order-create"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :list-orders="getPurchaseOrders"
    :get-detail="getPurchaseOrderDetail"
    :cancel-order="cancelPurchaseOrder"
  />
</template>

<script setup>
import { getProducts } from '../../api/product'
import { cancelPurchaseOrder, getPurchaseOrderDetail, getPurchaseOrders } from '../../api/purchase'
import { getSuppliers } from '../../api/supplier'
import OrderQueryPage from '../business/components/OrderQueryPage.vue'

function loadSubjects() {
  return getSuppliers()
}
</script>
