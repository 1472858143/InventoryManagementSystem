<template>
  <article
    class="product-card"
    :class="{ 'is-offline': !isOnSale }"
    role="button"
    tabindex="0"
    :aria-label="`查看${product.productName || '商品'}详情`"
    @click="emit('click', product)"
    @keydown.enter.prevent="emit('click', product)"
    @keydown.space.prevent="emit('click', product)"
  >
    <div class="card-top">
      <div class="title-block">
        <h3 :title="product.productName">{{ product.productName || '未命名商品' }}</h3>
        <span>{{ product.productCode || '系统编号' }}</span>
      </div>
      <span class="status-pill">{{ statusText }}</span>
    </div>

    <div class="meta-row">
      <span class="category-chip" :title="product.categoryName">{{ product.categoryName || '未分类' }}</span>
      <span class="unit-text">{{ product.unit || '-' }}</span>
    </div>

    <div class="price-row">
      <div>
        <span class="price-label">售价</span>
        <strong>¥ {{ formatPrice(product.salePrice) }}</strong>
      </div>
      <div>
        <span class="price-label">进价</span>
        <span class="purchase-price">¥ {{ formatPrice(product.purchasePrice) }}</span>
      </div>
    </div>

    <div class="card-footer">
      <span>销量 {{ Number(product.salesCount ?? 0) }} 件</span>
      <span>{{ formatDate(product.createTime) }}</span>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['click'])

const isOnSale = computed(() => props.product.status === 1)
const statusText = computed(() => (isOnSale.value ? '上架' : '下架'))

function formatPrice(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function formatDate(value) {
  if (!value) return '未记录'
  return String(value).slice(0, 10)
}
</script>

<style scoped>
.product-card {
  min-height: 172px;
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-left: 4px solid var(--color-success);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  cursor: pointer;
  transition: box-shadow 0.18s ease, transform 0.18s ease, border-color 0.18s ease;
  outline: none;
}

.product-card:hover,
.product-card:focus-visible {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
  border-color: var(--color-primary);
  border-left-color: var(--color-success);
}

.product-card.is-offline {
  border-left-color: var(--color-text-disabled);
  opacity: 0.72;
}

.product-card.is-offline:hover,
.product-card.is-offline:focus-visible {
  border-left-color: var(--color-text-disabled);
}

.card-top,
.price-row,
.card-footer {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.title-block {
  min-width: 0;
}

.title-block h3 {
  margin: 0 0 5px;
  font-size: 16px;
  line-height: 1.35;
  font-weight: 600;
  color: var(--color-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-block span,
.unit-text,
.price-label,
.card-footer {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.status-pill {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-success);
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.18);
  border-radius: var(--radius-sm);
  padding: 3px 8px;
}

.is-offline .status-pill {
  color: var(--color-text-secondary);
  background: var(--color-border-light);
  border-color: var(--color-border);
}

.meta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.category-chip {
  max-width: 70%;
  border-radius: var(--radius-sm);
  background: var(--color-primary-bg);
  color: var(--color-primary);
  padding: 4px 8px;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-row {
  align-items: flex-end;
}

.price-row > div {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-row strong {
  font-size: 22px;
  line-height: 1;
  color: var(--color-text);
}

.purchase-price {
  font-size: 14px;
  color: var(--color-text-secondary);
}

.card-footer {
  margin-top: auto;
  padding-top: 10px;
  border-top: 1px solid var(--color-border-light);
}
</style>
