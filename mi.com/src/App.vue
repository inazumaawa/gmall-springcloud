<template>
  <Header></Header>
  <router-view></router-view>
  <Footer></Footer>
  <AIChat />
</template>

<script setup>
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
import AIChat from './components/AIChat.vue'
</script>

<style>

</style>