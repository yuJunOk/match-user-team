<script setup lang="ts">
  import type {UserType} from "../models/user";

  interface UserCardListProps {
    userList: UserType[]
  }
  let props = withDefaults(defineProps<UserCardListProps>(), {
    //@ts-ignore
    userList: [] as UserType[]
  });
</script>

<template>
  <van-card
      v-for="item in userList"
      :desc="item.profile"
      :title="item.userName"
      :thumb="item.avatarUrl"
      tag="123"
  >
    <template #tags>
      <van-tag plain type="primary" v-for="tag in item.tags" style="margin-right: 8px; margin-top: 8px;">{{ tag }}
      </van-tag>
    </template>
    <template #footer>
      <van-button size="mini">联系我</van-button>
    </template>
  </van-card>
</template>

<style scoped>

</style>