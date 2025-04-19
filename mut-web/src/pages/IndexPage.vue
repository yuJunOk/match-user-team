<script setup lang="ts">
import {ref, watchEffect} from "vue";
import myAxios from "../plugins/MyAxios.ts";
import UserCardList from "../components/UserCardList.vue";

const userList = ref([])

const isMatchMode = ref(false);
const loading = ref(true);

/**
 * 加载数据
 */
const loadData = async () => {
  let userListData;
  loading.value = true;
  // 匹配模式，根据标签匹配用户
  if (isMatchMode.value) {
    const num = 10;
    userListData = await myAxios.get('user/match', {
      params: {
        num
      }
    }).then(res => {
      console.log(res);
      showSuccessToast('请求成功');
      return res.data;
    }).catch(err => {
      console.log(err);
      showFailToast('请求失败');
    });
  } else {
    // 普通模式，直接分页查询用户
    userListData = await myAxios.get('user/recommend', {
      params: {
        pageSize: 10,
        current: 1,
      }
    }).then(res => {
      console.log(res);
      showSuccessToast('请求成功');
      return res.data.records;
    }).catch(err => {
      console.log(err);
      showFailToast('请求失败');
    });
  }
  if (userListData) {
    userListData.forEach((user: UserType) => {
      if (user.tags) {
        user.tags = JSON.parse(user.tags);
      }
    })
    userList.value = userListData;
  }
  loading.value = false;
}

watchEffect(() => {
  loadData();
})
</script>

<template>
  <van-cell center title="推荐模式">
    <template #right-icon>
      <van-switch v-model="isMatchMode" size="24"/>
    </template>
  </van-cell>
  <user-card-list :user-list="userList" :loading="loading"/>
  <van-empty v-if="!userList || userList.length === 0" description="数据为空"></van-empty>
</template>

<style scoped>

</style>