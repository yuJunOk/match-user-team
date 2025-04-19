<script setup>
import { ref } from 'vue';
import {useRouter} from "vue-router";

const router = useRouter();

const searchText = ref('');
const onSearch = (val) => {
  // showToast(val);
  tagList.value = originTagList.map(tag => {
    const tempChildren = [...tag.children];
    const tempTag = {...tag};
    tempTag.children = tempChildren.filter(item => item.text.includes(val));
    return tempTag;
  });
};
const onCancel = () => {
  // showToast('取消');
  searchText.value = '';
  onSearch('');
};

//已选中的标签
const activeIds = ref([]);
//
const activeIndex = ref(0);
// 标签列表
let originTagList = [
  {
    text: '语言',
    children: [
      { text: 'Java', id: 'Java' },
      { text: 'Python', id: 'Python' },
      { text: 'C', id: 'C' },
    ],
  }
];
let tagList = ref(originTagList);

// 移除标签
const doClose = (tag) => {
  activeIds.value = activeIds.value.filter((v) => v !== tag);
}

//
const doSearchResult = () => {
  router.push({
    path: '/user/list',
    query: {
      tags: activeIds.value,
    }
  });
}
</script>

<template>
  <form action="/">
    <van-search
        v-model="searchText"
        show-action
        placeholder="请输入搜索关键词"
        @search="onSearch"
        @cancel="onCancel"
    />
  </form>
  <van-divider content-position="left">已选标签</van-divider>
  <van-row gutter="16" style="padding: 0 16px;">
    <van-col v-for="tag in activeIds">
      <van-tag closeable size="medium" type="primary" @close="doClose(tag)">
        {{tag}}
      </van-tag>
    </van-col>
    <div v-if="activeIds.length === 0">请选择标签</div>
  </van-row>
  <van-divider content-position="left">选择标签</van-divider>
  <van-tree-select
      v-model:active-id="activeIds"
      v-model:main-active-index="activeIndex"
      :items="tagList"
  />
  <div style="padding: 20px;">
    <van-button type="primary" @click="doSearchResult" block>搜索</van-button>
  </div>
</template>

<style scoped>

</style>