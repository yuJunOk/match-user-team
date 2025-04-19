<script setup lang="ts">

  import {useRouter} from "vue-router";
  import TeamCardList from "../components/TeamCardList.vue";
  import {onMounted, ref} from "vue";
  import myAxios from "../plugins/MyAxios.ts";
  import {showFailToast} from "vant";

  const router = useRouter();

  const doCreateTeam = () => {
    router.push("/team/add");
  }

  /**
   * 搜索队伍
   * @param val
   * @returns {Promise<void>}
   */
  const listTeam = async (val = '', status = 0) => {
    const res = await myAxios.get("/team/list", {
      params: {
        searchText: val,
        pageNum: 1,
        status
      },
    });
    if (res?.code === 23200) {
      teamList.value = res.data;
    } else {
      showFailToast('加载队伍失败，请刷新重试');
    }
  }

  const teamList = ref([]);
  onMounted(() => {
    listTeam();
  })

  const searchText = ref("");
  const onSearch = () => {
    listTeam(searchText.value);
  }

  const active = ref();
  const onTabChange = (name) =>{
    if(name === 'public') {
      listTeam(searchText.value, 0);
    }else {
      listTeam(searchText.value, 2);
    }
  }
</script>

<template>
  <div id="team-page">
    <van-search v-model="searchText" placeholder="搜索队伍" @search="onSearch"/>
    <van-tabs v-model:active="active" @change="onTabChange">
      <van-tab title="公开" name="public"></van-tab>
      <van-tab title="加密" name="private"></van-tab>
    </van-tabs>
    <van-button class="add-button" type="primary" icon="plus" @click="doCreateTeam"></van-button>
    <team-card-list :team-list="teamList"/>
    <van-empty v-if="teamList?.length === 0" description="数据为空"/>
  </div>
</template>

<style scoped>

</style>