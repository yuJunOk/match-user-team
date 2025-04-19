<script setup lang="ts">
import {onMounted, ref} from "vue";
  import {getCurrentUser} from "../services/user.ts";

  let user = ref();

  onMounted(async () => {
    const res = await getCurrentUser();
    if (res) {
      user.value = res;
    }
  })
</script>

<template>
  <div v-if="user">
    <van-cell title="昵称" is-link to="/user/edit" :value="user.userName"/>
    <van-cell title="修改信息" is-link to="/user/update" />
    <van-cell title="我创建的队伍" is-link to="/user/team/create" />
    <van-cell title="我加入的队伍" is-link to="/user/team/join" />
  </div>
  <van-empty v-if="!user" description="用户未登录"/>
</template>

<style scoped>

</style>