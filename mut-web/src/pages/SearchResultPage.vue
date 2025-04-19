<script setup>
import {useRoute} from "vue-router";
import {ref, onMounted} from "vue";
import myAxios from "../plugins/MyAxios.ts";
import qs from "qs"
import UserCardList from "../components/UserCardList.vue";

const route = useRoute();
const {tags} = route.query;

onMounted(async () => {
  const userListData = await myAxios.get('user/search/tags', {
    params: {
      tagList: tags
    },
    paramsSerializer: params => {
      return qs.stringify(params, {indices: false});
    }
  }).then(res => {
    console.log(res);
    showSuccessToast('请求成功');
    return res.data;
  }).catch(err => {
    console.log(err);
    showFailToast('请求失败');
  });

  if (userListData && userListData.length > 0) {
    userListData.forEach((item) => {
      if (item.tags){
        item.tags = JSON.parse(item.tags);
      }
    })
    userList.value = userListData;
  }

  console.log(userList.value);
})

const userList = ref([])
</script>

<template>
  <user-card-list :user-list="userList"/>
  <van-empty v-if="!userList || userList.length === 0" description="搜索结果为空"></van-empty>
</template>

<style scoped>

</style>