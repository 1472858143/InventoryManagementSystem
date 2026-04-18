<template>
  <AdminLayout>
    <section class="home-placeholder">
      <h1>首页</h1>
      <p class="home-placeholder__welcome">欢迎使用超市库存管理系统</p>
      <p class="home-placeholder__description">
        后续用户、商品、库存、入库、出库、盘点、报表和系统页面会显示在这里
      </p>
    </section>
  </AdminLayout>
</template>

<script setup>
import AdminLayout from './layout/AdminLayout.vue'
</script>

<style scoped>
.home-placeholder {
  min-height: 240px;
  padding: 24px;
  border: 1px dashed #9ca3af;
  border-radius: 8px;
  background: #ffffff;
}

.home-placeholder h1 {
  margin: 0 0 12px;
  font-size: 24px;
  color: #111827;
}

.home-placeholder p {
  margin: 0;
  color: #4b5563;
}

.home-placeholder__welcome {
  margin-bottom: 8px;
  font-size: 18px;
  color: #1f2937;
}

.home-placeholder__description {
  line-height: 1.7;
}
</style>
