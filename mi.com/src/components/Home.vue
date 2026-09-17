<template>
  <main class="home">
    <Banner />
    <GoodList />
  </main>
</template>

<script>
import Banner from './Banner.vue'
import GoodList from './GoodList.vue'
export default {
  components: {
    Banner,
    GoodList
  }
}
</script>

<style>
.home { display: block; }
</style>
