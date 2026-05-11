<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p>以卡片方式浏览商品，快速检索、筛选并管理上下架状态</p>
      </div>
      <el-button :icon="RefreshCw" :loading="loading" @click="loadProducts">刷新</el-button>
    </div>

    <ProductFilterBar
      :categories="categories"
      @change="handleFilterChange"
      @create="openDialog"
    />

    <div class="product-summary">
      <span>共 {{ products.length }} 个商品</span>
      <span>当前显示 {{ filteredAndSortedProducts.length }} 个</span>
    </div>

    <div class="product-grid" v-loading="loading">
      <ProductCard
        v-for="product in filteredAndSortedProducts"
        :key="product.id"
        :product="product"
        @click="openDetail"
      />
      <el-empty
        v-if="!loading && filteredAndSortedProducts.length === 0"
        class="empty-state"
        description="暂无匹配商品"
      />
    </div>

    <ProductDetailDrawer
      v-model:visible="drawerVisible"
      :product="selectedProduct"
      @status-changed="handleStatusChanged"
    />

    <el-dialog
      v-model="dialogVisible"
      title="新增商品"
      width="520px"
      :close-on-click-modal="false"
      @open="handleDialogOpen"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model.trim="form.productName" autocomplete="off" placeholder="如：矿泉水" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" filterable placeholder="请选择分类" style="width:100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.categoryName" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-select v-model="form.unit" filterable allow-create placeholder="请选择或输入单位" style="width:100%">
            <el-option v-for="u in unitOptions" :key="u" :label="u" :value="u" />
          </el-select>
        </el-form-item>
        <el-form-item label="进价" prop="purchasePrice">
          <el-input-number v-model="form.purchasePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
        <el-form-item label="售价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" :step="0.1" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { RefreshCw } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { createProduct, getProducts } from '../../api/product'
import { getEnabledCategories } from '../../api/category'
import { compareByPinyinInitials } from '../../utils/pinyinSort'
import ProductCard from './components/ProductCard.vue'
import ProductDetailDrawer from './components/ProductDetailDrawer.vue'
import ProductFilterBar from './components/ProductFilterBar.vue'

const products = ref([])
const categories = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const submitting = ref(false)
const selectedProduct = ref(null)
const formRef = ref(null)
const unitOptions = ['件', '箱', 'kg', 'g', 'L', 'mL', '瓶', '袋']
const form = reactive({ productName: '', categoryId: null, unit: '件', purchasePrice: null, salePrice: null })
const filterState = ref({
  keyword: '',
  salePriceRange: [null, null],
  purchasePriceRange: [null, null],
  categoryIds: [],
  status: 'all',
  sortKey: 'salesDesc',
})

const rules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  unit: [{ required: true, message: '请选择或输入单位', trigger: 'change' }],
  purchasePrice: [{ required: true, message: '请输入进价', trigger: 'change', type: 'number' }],
  salePrice: [{ required: true, message: '请输入售价', trigger: 'change', type: 'number' }],
}

const filteredAndSortedProducts = computed(() => {
  const filters = filterState.value
  const keyword = filters.keyword.trim()
  const filtered = products.value.filter(product => {
    const matchesKeyword = !keyword
      || String(product.productName ?? '').includes(keyword)
      || String(product.categoryName ?? '').includes(keyword)
    const matchesSalePrice = isInRange(product.salePrice, filters.salePriceRange)
    const matchesPurchasePrice = isInRange(product.purchasePrice, filters.purchasePriceRange)
    const matchesCategory = filters.categoryIds.length === 0 || filters.categoryIds.includes(product.categoryId)
    const matchesStatus = filters.status === 'all' || product.status === filters.status
    return matchesKeyword && matchesSalePrice && matchesPurchasePrice && matchesCategory && matchesStatus
  })

  return [...filtered].sort((left, right) => compareProducts(left, right, filters.sortKey))
})

onMounted(() => {
  loadProducts()
  loadCategories()
})

async function loadProducts() {
  loading.value = true
  try {
    const result = await getProducts()
    products.value = Array.isArray(result) ? result : []
    syncSelectedProduct()
  } catch (e) {
    ElMessage.error(e.message || '查询商品列表失败')
    products.value = []
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const result = await getEnabledCategories()
    categories.value = Array.isArray(result) ? result : []
  } catch {
    categories.value = []
  }
}

function handleFilterChange(nextFilters) {
  filterState.value = nextFilters
}

function openDialog() {
  dialogVisible.value = true
}

function openDetail(product) {
  selectedProduct.value = product
  drawerVisible.value = true
}

async function handleStatusChanged() {
  await loadProducts()
}

async function handleDialogOpen() {
  Object.assign(form, {
    productName: '',
    categoryId: null,
    unit: '件',
    purchasePrice: null,
    salePrice: null,
  })
  await loadCategories()
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createProduct({
      productName: form.productName,
      categoryId: form.categoryId,
      unit: form.unit,
      purchasePrice: form.purchasePrice,
      salePrice: form.salePrice,
    })
    ElMessage.success('新增商品成功')
    dialogVisible.value = false
    await loadProducts()
  } catch (e) {
    ElMessage.error(e.message || '新增商品失败')
  } finally {
    submitting.value = false
  }
}

function compareProducts(left, right, sortKey) {
  if (sortKey === 'nameAsc') {
    return compareByPinyinInitials(left.productName, right.productName)
  }
  if (sortKey === 'categoryAsc') {
    return compareByPinyinInitials(left.categoryName, right.categoryName)
  }
  if (sortKey === 'priceAsc') {
    return toNumber(left.salePrice) - toNumber(right.salePrice)
  }
  if (sortKey === 'priceDesc') {
    return toNumber(right.salePrice) - toNumber(left.salePrice)
  }
  return Number(right.salesCount ?? 0) - Number(left.salesCount ?? 0)
}

function isInRange(value, range) {
  const number = toNumber(value)
  const [min, max] = range
  if (min !== null && min !== undefined && number < Number(min)) return false
  if (max !== null && max !== undefined && number > Number(max)) return false
  return true
}

function toNumber(value) {
  const number = Number(value ?? 0)
  return Number.isFinite(number) ? number : 0
}

function syncSelectedProduct() {
  if (!selectedProduct.value) return
  const nextSelected = products.value.find(product => product.id === selectedProduct.value.id)
  if (nextSelected) {
    selectedProduct.value = nextSelected
  }
}
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 4px;
}

.page-header p {
  font-size: 13px;
  color: var(--color-text-secondary);
}

.product-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--color-text-secondary);
  font-size: 13px;
}

.product-grid {
  min-height: 260px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
}

.empty-state {
  grid-column: 1 / -1;
  min-height: 240px;
  background: var(--color-white);
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
}

@media (max-width: 640px) {
  .page-header {
    align-items: stretch;
    flex-direction: column;
  }

  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>
