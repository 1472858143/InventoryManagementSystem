<template>
  <OrderFormPage
    title="采购退货"
    description="登记退回供应商的商品，保存后自动扣减仓库库存"
    subject-label="供应商"
    subject-placeholder="请选择供应商"
    subject-key="supplierId"
    source-order-key="sourceOrderId"
    source-order-label="原进货单ID"
    remark-key="reason"
    remark-label="退货原因"
    price-field="purchasePrice"
    success-route-name="purchase-returns"
    success-message="采购退货单保存成功"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :create-order="createPurchaseReturn"
  />
</template>

<script setup>
import { getProducts } from '../../api/product'
import { createPurchaseReturn } from '../../api/purchaseReturn'
import { getSuppliers } from '../../api/supplier'
import OrderFormPage from '../business/components/OrderFormPage.vue'

function loadSubjects() {
  return getSuppliers()
}
</script>
