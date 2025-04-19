<script setup lang="ts">
import {useRouter} from "vue-router";
import {ref} from "vue";
import myAxios from "../plugins/MyAxios.ts";
import {showFailToast, showSuccessToast} from "vant";

const router = useRouter();
const route = useRouter();

const loginName = ref('');
const loginPwd = ref('');
const onSubmit = async () => {
  const res = await myAxios.post("/user/login", {
    loginName: loginName.value,
    loginPwd: loginPwd.value,
  })
  if (res.code === 23200) {
    showSuccessToast('登录成功');
    //跳转到之前的页面
    const redirect = route.query?.redirect ?? '/';
    router.replace(redirect);
  }else {
    showFailToast('登录失败');
  }
}
</script>

<template>
  <van-form @submit="onSubmit">
    <van-cell-group inset>
      <van-field
          v-model="loginName"
          name="loginName"
          label="用户名"
          placeholder="用户名"
          :rules="[{ required: true, message: '请填写用户名' }]"
      />
      <van-field
          v-model="loginPwd"
          type="password"
          name="loginPwd"
          label="密码"
          placeholder="密码"
          :rules="[{ required: true, message: '请填写密码' }]"
      />
    </van-cell-group>
    <div style="margin: 16px;">
      <van-button round block type="primary" native-type="submit">
        登录
      </van-button>
    </div>
  </van-form>
</template>

<style scoped>

</style>