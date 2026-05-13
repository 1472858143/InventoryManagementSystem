<template>
  <OrderFormPage
    title="进货入库"
    description="登记供应商进货单，保存后自动增加仓库库存"
    subject-label="供应商"
    subject-placeholder="请选择供应商"
    subject-key="supplierId"
    price-field="purchasePrice"
    success-route-name="purchase-orders"
    success-message="进货单保存成功"
    :load-subjects="loadSubjects"
    :load-products="getProducts"
    :create-order="createPurchaseOrder"
  />
</template>

<script setup>
import { getProducts } from '../../api/product'
import { createPurchaseOrder } from '../../api/purchase'
import { getSuppliers } from '../../api/supplier'
import OrderFormPage from '../business/components/OrderFormPage.vue'

function loadSubjects() {
  return getSuppliers()
}
</script>
