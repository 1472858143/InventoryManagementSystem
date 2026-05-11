<template>
  <el-drawer
    :model-value="visible"
    :size="drawerSize"
    direction="rtl"
    class="product-drawer"
    :with-header="false"
    @update:model-value="emit('update:visible', $event)"
  >
    <template v-if="product">
      <div class="drawer-header">
        <div>
          <h2>{{ product.productName || '未命名商品' }}</h2>
          <p>{{ product.productCode || '系统编号' }}</p>
        </div>
        <div class="drawer-actions">
          <el-tag :type="isOnSale ? 'success' : 'info'" effect="light">{{ isOnSale ? '上架' : '下架' }}</el-tag>
          <el-button
            class="close-button"
            :icon="X"
            circle
            text
            title="关闭"
            aria-label="关闭"
            @click="emit('update:visible', false)"
          />
        </div>
      </div>

      <section class="detail-section">
        <h3>商品信息</h3>
        <dl class="detail-grid">
          <div>
            <dt>商品编号</dt>
            <dd>{{ product.productCode || '-' }}</dd>
          </div>
          <div>
            <dt>分类</dt>
            <dd>{{ product.categoryName || '-' }}</dd>
          </div>
          <div>
            <dt>单位</dt>
            <dd>{{ product.unit || '-' }}</dd>
          </div>
          <div>
            <dt>进价</dt>
            <dd>¥ {{ formatPrice(product.purchasePrice) }}</dd>
          </div>
          <div>
            <dt>售价</dt>
            <dd>¥ {{ formatPrice(product.salePrice) }}</dd>
          </div>
          <div>
            <dt>销量</dt>
            <dd>{{ Number(product.salesCount ?? 0) }} 件</dd>
          </div>
          <div class="wide">
            <dt>创建时间</dt>
            <dd>{{ formatDateTime(product.createTime) }}</dd>
          </div>
        </dl>
      </section>

      <section class="detail-section">
        <h3>状态管理</h3>
        <div class="status-panel">
          <div>
            <span class="status-label">当前状态</span>
            <strong>{{ isOnSale ? '上架销售中' : '已下架' }}</strong>
          </div>
          <el-button
            :type="isOnSale ? 'warning' : 'primary'"
            :loading="updating"
            @click="handleToggleStatus"
          >
            {{ isOnSale ? '下架商品' : '上架商品' }}
          </el-button>
        </div>
      </section>

      <!-- 扩展挂载点：折扣管理、促销活动、关联库存、操作历史。 -->
    </template>

    <el-empty v-else description="请选择商品" />
  </el-drawer>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { X } from 'lucide-vue-next'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { updateProductStatus } from '../../../api/product'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  product: {
    type: Object,
    default: null,
  },
})

const emit = defineEmits(['update:visible', 'status-changed'])

const updating = ref(false)
const viewportWidth = ref(typeof window === 'undefined' ? 1280 : window.innerWidth)
const isOnSale = computed(() => props.product?.status === 1)
const drawerSize = computed(() => (viewportWidth.value < 600 ? '92%' : '520px'))

function handleResize() {
  viewportWidth.value = window.innerWidth
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

async function handleToggleStatus() {
  if (!props.product) return
  updating.value = true
  try {
    await updateProductStatus({
      productId: props.product.id,
      status: props.product.status === 1 ? 0 : 1,
    })
    ElMessage.success('状态更新成功')
    emit('status-changed')
  } catch (e) {
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    updating.value = false
  }
}

function formatPrice(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number) ? number.toFixed(2) : '0.00'
}

function formatDateTime(value) {
  if (!value) return '-'
  return String(value).slice(0, 19).replace('T', ' ')
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--color-border-light);
}

.drawer-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  line-height: 1.3;
  color: var(--color-text);
}

.drawer-header p {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.drawer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.close-button {
  color: var(--color-text-secondary);
}

.detail-section {
  margin-top: 18px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 16px;
  background: var(--color-white);
}

.detail-section h3 {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text);
}

.detail-grid {
  margin: 0;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-grid div {
  min-width: 0;
}

.detail-grid .wide {
  grid-column: 1 / -1;
}

.detail-grid dt,
.status-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  margin-bottom: 5px;
}

.detail-grid dd {
  margin: 0;
  color: var(--color-text);
  font-size: 14px;
  overflow-wrap: anywhere;
}

.status-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.status-panel strong {
  display: block;
  color: var(--color-text);
  font-size: 17px;
  line-height: 1.3;
}

@media (max-width: 480px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .status-panel {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
