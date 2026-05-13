<template>
  <el-drawer
    :model-value="modelValue"
    :title="title"
    size="620px"
    @update:model-value="value => emit('update:modelValue', value)"
  >
    <template v-if="order">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="单据号">{{ order[numberField] }}</el-descriptions-item>
        <el-descriptions-item :label="subjectLabel">{{ subjectName }}</el-descriptions-item>
        <el-descriptions-item v-if="sourceField" label="来源单">{{ order[sourceField] || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 'NORMAL' ? 'success' : 'info'" size="small">
            {{ order.status === 'NORMAL' ? '正常' : '已作废' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="合计数量">{{ order.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="合计金额">¥{{ amountText(order.totalAmount) }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ order.operatorId || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="remarkLabel">{{ order[remarkField] || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="order.items || []" border class="detail-table" empty-text="暂无明细">
        <el-table-column label="商品" min-width="220">
          <template #default="{ row }">{{ productName(row.productId) }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column label="单价" width="110">
          <template #default="{ row }">¥{{ amountText(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">¥{{ amountText(row.subtotal) }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="140">
          <template #default="{ row }">{{ row.remark || '-' }}</template>
        </el-table-column>
      </el-table>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  products: { type: Array, default: () => [] },
  subjects: { type: Array, default: () => [] },
  title: { type: String, default: '单据详情' },
  subjectLabel: { type: String, default: '往来单位' },
  subjectField: { type: String, required: true },
  numberField: { type: String, required: true },
  sourceField: { type: String, default: '' },
  remarkField: { type: String, default: 'remark' },
  remarkLabel: { type: String, default: '备注' },
})

const emit = defineEmits(['update:modelValue'])

const subjectName = computed(() => {
  const subject = props.subjects.find(item => item.id === props.order?.[props.subjectField])
  return subject?.name || props.order?.[props.subjectField] || '-'
})

function productName(productId) {
  const product = props.products.find(item => item.id === productId)
  return product ? `${product.productCode || product.id} - ${product.productName}` : productId
}

function amountText(value) {
  return Number(value || 0).toFixed(2)
}
</script>

<style scoped>
.detail-table { margin-top: 16px; }
</style>
