<template>
  <div class="aftersales-section">
    <div class="section-head">
      <h2 class="head-title">售后/退款</h2>
    </div>
    <div class="empty-state">
      <p>售后功能开发中...</p>
      <router-link to="/center/myorder" class="btn-primary" style="margin-top: var(--s-16)">查看订单</router-link>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.aftersales-section { margin: 0; }
.section-head {
  margin-bottom: var(--s-20);
  padding-bottom: var(--s-16);
  border-bottom: 1px solid var(--c-hairline);
}
.head-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-ink);
  margin: 0;
}
</style>
