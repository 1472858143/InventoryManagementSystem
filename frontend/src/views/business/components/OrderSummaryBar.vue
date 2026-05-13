<template>
  <div class="summary-bar">
    <div>
      <span class="label">明细行数</span>
      <strong>{{ rowCount }}</strong>
    </div>
    <div>
      <span class="label">合计数量</span>
      <strong>{{ totalQuantity }}</strong>
    </div>
    <div>
      <span class="label">合计金额</span>
      <strong>¥{{ totalAmountText }}</strong>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  items: { type: Array, default: () => [] },
})

const rowCount = computed(() => props.items.length)
const totalQuantity = computed(() => props.items.reduce((sum, item) => sum + Number(item.quantity || 0), 0))
const totalAmountText = computed(() => props.items
  .reduce((sum, item) => sum + Number(item.quantity || 0) * Number(item.unitPrice || 0), 0)
  .toFixed(2))
</script>

<style scoped>
.summary-bar {
  display: flex;
  justify-content: flex-end;
  gap: 24px;
  padding: 12px 0 0;
  color: var(--color-text);
}
.summary-bar > div { display: flex; align-items: center; gap: 8px; }
.label { color: var(--color-text-secondary); font-size: 13px; }
strong { font-size: 15px; }
</style>
