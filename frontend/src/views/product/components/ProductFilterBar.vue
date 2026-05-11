<template>
  <div class="filter-bar">
    <div class="filter-main">
      <el-input
        v-model="filters.keyword"
        class="keyword-input"
        :prefix-icon="Search"
        clearable
        placeholder="搜索商品名称或分类"
      />
      <el-select v-model="filters.sortKey" class="sort-select" placeholder="排序">
        <el-option label="销量从高到低" value="salesDesc" />
        <el-option label="商品名 A-Z" value="nameAsc" />
        <el-option label="分类 A-Z" value="categoryAsc" />
        <el-option label="售价从低到高" value="priceAsc" />
        <el-option label="售价从高到低" value="priceDesc" />
      </el-select>
      <el-button :icon="SlidersHorizontal" @click="filtersExpanded = !filtersExpanded">筛选</el-button>
      <el-button type="primary" :icon="Plus" @click="emit('create')">新增商品</el-button>
    </div>

    <el-collapse-transition>
      <div v-show="filtersExpanded" class="filter-panel">
        <div class="range-group">
          <span>售价</span>
          <el-input-number v-model="filters.minSalePrice" :min="0" :precision="2" controls-position="right" placeholder="最低" />
          <span class="dash">-</span>
          <el-input-number v-model="filters.maxSalePrice" :min="0" :precision="2" controls-position="right" placeholder="最高" />
        </div>
        <div class="range-group">
          <span>进价</span>
          <el-input-number v-model="filters.minPurchasePrice" :min="0" :precision="2" controls-position="right" placeholder="最低" />
          <span class="dash">-</span>
          <el-input-number v-model="filters.maxPurchasePrice" :min="0" :precision="2" controls-position="right" placeholder="最高" />
        </div>
        <el-select
          v-model="filters.categoryIds"
          class="category-select"
          multiple
          collapse-tags
          collapse-tags-tooltip
          clearable
          placeholder="分类"
        >
          <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
        </el-select>
        <el-radio-group v-model="filters.status" class="status-group">
          <el-radio-button value="all">全部</el-radio-button>
          <el-radio-button :value="1">上架</el-radio-button>
          <el-radio-button :value="0">下架</el-radio-button>
        </el-radio-group>
        <el-button :icon="RotateCcw" @click="resetFilters">重置</el-button>
      </div>
    </el-collapse-transition>
  </div>
</template>

<script setup>
import { Plus, RotateCcw, Search, SlidersHorizontal } from 'lucide-vue-next'
import { onMounted, onUnmounted, reactive, ref, watch } from 'vue'

defineProps({
  categories: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['change', 'create'])

const filtersExpanded = ref(false)
const filters = reactive({
  keyword: '',
  minSalePrice: null,
  maxSalePrice: null,
  minPurchasePrice: null,
  maxPurchasePrice: null,
  categoryIds: [],
  status: 'all',
  sortKey: 'salesDesc',
})

let emitTimer = null

watch(filters, scheduleEmit, { deep: true })

onMounted(() => {
  emitChange()
})

onUnmounted(() => {
  clearTimeout(emitTimer)
})

function scheduleEmit() {
  clearTimeout(emitTimer)
  emitTimer = setTimeout(emitChange, 300)
}

function emitChange() {
  emit('change', {
    keyword: filters.keyword.trim(),
    salePriceRange: [filters.minSalePrice, filters.maxSalePrice],
    purchasePriceRange: [filters.minPurchasePrice, filters.maxPurchasePrice],
    categoryIds: [...filters.categoryIds],
    status: filters.status,
    sortKey: filters.sortKey,
  })
}

function resetFilters() {
  Object.assign(filters, {
    keyword: '',
    minSalePrice: null,
    maxSalePrice: null,
    minPurchasePrice: null,
    maxPurchasePrice: null,
    categoryIds: [],
    status: 'all',
    sortKey: 'salesDesc',
  })
  clearTimeout(emitTimer)
  emitChange()
}
</script>

<style scoped>
.filter-bar {
  background: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 14px;
  box-shadow: var(--shadow-card);
}

.filter-main {
  display: flex;
  align-items: center;
  gap: 10px;
}

.keyword-input {
  width: 320px;
  max-width: 100%;
}

.sort-select {
  width: 168px;
}

.filter-main .el-button:last-child {
  margin-left: auto;
}

.filter-panel {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.range-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.range-group > span:first-child {
  color: var(--color-text-secondary);
  font-size: 13px;
  white-space: nowrap;
}

.range-group :deep(.el-input-number) {
  width: 118px;
}

.dash {
  color: var(--color-text-disabled);
}

.category-select {
  width: 190px;
}

@media (max-width: 860px) {
  .filter-main,
  .filter-panel {
    align-items: stretch;
    flex-direction: column;
  }

  .keyword-input,
  .sort-select,
  .category-select,
  .filter-main .el-button {
    width: 100%;
  }

  .filter-main .el-button:last-child {
    margin-left: 0;
  }

  .range-group {
    display: grid;
    grid-template-columns: 40px 1fr 12px 1fr;
  }

  .range-group :deep(.el-input-number) {
    width: 100%;
  }
}
</style>
