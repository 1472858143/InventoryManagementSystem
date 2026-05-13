<template>
  <div class="item-editor">
    <div class="item-toolbar">
      <el-button type="primary" :icon="Plus" @click="addRow">添加明细</el-button>
      <el-text v-if="hasDuplicate" type="danger">同一张单据不能重复选择商品</el-text>
    </div>

    <el-table :data="rows" border row-key="rowKey" empty-text="暂无商品明细">
      <el-table-column label="商品" min-width="240">
        <template #default="{ row }">
          <el-select
            :model-value="row.productId"
            filterable
            placeholder="选择商品"
            style="width:100%"
            @update:model-value="value => updateProduct(row.rowKey, value)"
          >
            <el-option
              v-for="product in enabledProducts"
              :key="product.id"
              :label="productLabel(product)"
              :value="product.id"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="150">
        <template #default="{ row }">
          <el-input-number
            :model-value="row.quantity"
            :min="1"
            :precision="0"
            controls-position="right"
            style="width:100%"
            @update:model-value="value => updateRow(row.rowKey, { quantity: value })"
          />
        </template>
      </el-table-column>
      <el-table-column label="单价" width="150">
        <template #default="{ row }">
          <el-input-number
            :model-value="row.unitPrice"
            :min="0"
            :precision="2"
            controls-position="right"
            style="width:100%"
            @update:model-value="value => updateRow(row.rowKey, { unitPrice: value })"
          />
        </template>
      </el-table-column>
      <el-table-column label="小计" width="130">
        <template #default="{ row }">¥{{ subtotal(row) }}</template>
      </el-table-column>
      <el-table-column label="备注" min-width="160">
        <template #default="{ row }">
          <el-input
            :model-value="row.remark"
            placeholder="选填"
            @update:model-value="value => updateRow(row.rowKey, { remark: value })"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="88" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" :icon="Trash2" @click="removeRow(row.rowKey)" />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { Plus, Trash2 } from 'lucide-vue-next'
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  products: { type: Array, default: () => [] },
  priceField: { type: String, default: 'purchasePrice' },
})

const emit = defineEmits(['update:modelValue'])

const rows = computed(() => props.modelValue)
const enabledProducts = computed(() => props.products.filter(product => product.status === 1))
const hasDuplicate = computed(() => {
  const selected = rows.value.map(row => row.productId).filter(Boolean)
  return new Set(selected).size !== selected.length
})

function productLabel(product) {
  return `${product.productCode || product.id} - ${product.productName || ''}`
}

function nextRowKey() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

function emitRows(nextRows) {
  emit('update:modelValue', nextRows)
}

function addRow() {
  emitRows([...rows.value, { rowKey: nextRowKey(), productId: null, quantity: 1, unitPrice: 0, remark: '' }])
}

function removeRow(rowKey) {
  emitRows(rows.value.filter(row => row.rowKey !== rowKey))
}

function updateRow(rowKey, patch) {
  emitRows(rows.value.map(row => row.rowKey === rowKey ? { ...row, ...patch } : row))
}

function updateProduct(rowKey, productId) {
  const product = props.products.find(item => item.id === productId)
  const unitPrice = Number(product?.[props.priceField] ?? 0)
  updateRow(rowKey, { productId, unitPrice })
}

function subtotal(row) {
  return (Number(row.quantity || 0) * Number(row.unitPrice || 0)).toFixed(2)
}
</script>

<style scoped>
.item-editor { display: flex; flex-direction: column; gap: 10px; }
.item-toolbar { display: flex; align-items: center; gap: 12px; min-height: 32px; }
</style>
