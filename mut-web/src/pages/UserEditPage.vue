<script setup lang="ts">
import {useRoute, useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/MyAxios.ts";
import {showSuccessToast} from "vant";
import {setCurrentUserStates} from "../states/user.ts";

const route = useRoute();
const router = useRouter();
const editUser = ref({
  id: route.query.editUserId,
  editKey: route.query.editKey,
  editName: route.query.editName,
  value: route.query.currentValue
})

const onSubmit = async (values: object) => {
  console.log('submit', values);
  const res = await myAxios.post('/user/update/self', {
    'id': 1,
    [editUser.value.editKey as string]: editUser.value.value
  })
  if (res.code === 23200){
    showSuccessToast('修改成功！');
    setCurrentUserStates(null);
    router.back();
  }
};
</script>

<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="editUser.value"
          :name="editUser.editKey"
          :label="editUser.editName"
          placeholder="`请输入${editUser.editName}`"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        提交
      </van-button>
    </div>
  </van-form>
</template>

<style scoped>

</style>