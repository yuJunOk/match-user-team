<template>
  <div id="team-update-page">
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
            v-model="addTeamData.name"
            name="name"
            label="队伍名"
            placeholder="请输入队伍名"
            :rules="[{ required: true, message: '请输入队伍名' }]"
        />
        <van-field
            v-model="addTeamData.description"
            rows="4"
            autosize
            label="队伍描述"
            type="textarea"
            placeholder="请输入队伍描述"
        />
        <van-field
            is-link
            readonly
            name="datetimePicker"
            label="过期时间"
            :placeholder="addTeamData.expireTime ?? '点击选择过期时间'"
            @click="showPicker = true"
        />
        <van-popup v-model:show="showPicker" position="bottom">
          <van-date-picker
              v-model="addTeamData.expireTime"
              @confirm="showPicker = false"
              type="datetime"
              title="请选择过期时间"
              :min-date="minDate"
          />
        </van-popup>
        <van-field name="radio" label="队伍状态">
          <template #input>
            <van-radio-group v-model="addTeamData.status" direction="horizontal">
              <van-radio name="0">公开</van-radio>
              <van-radio name="1">私有</van-radio>
              <van-radio name="2">加密</van-radio>
            </van-radio-group>
          </template>
        </van-field>
        <van-field
            v-if="Number(addTeamData.status) === 2"
            v-model="addTeamData.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入队伍密码"
            :rules="[{ required: true, message: '请填写密码' }]"
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">
          提交
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup lang="ts">

import {useRoute, useRouter} from "vue-router";
import myAxios from "../plugins/myAxios";
import {showFailToast, showSuccessToast} from "vant";
import {onMounted, ref} from "vue";

const router = useRouter();
const route = useRoute();
// 展示日期选择器
const showPicker = ref(false);

const minDate = new Date();

// 需要用户填写的表单数据
const addTeamData = ref({})

const id = route.query.id;

//获取之前队伍的信息
onMounted(async () => {
  if (id <= 0) {
    showFailToast("队伍加载失败");
    return;
  }
  const res = await myAxios.get("/team/get", {
    params: {
      id: id,
    }
  });
  if (res?.code === 23200) {
    addTeamData.value = res.data;
  } else {
    showFailToast("队伍加载失败，请刷新重试");
  }
})

// 提交
const onSubmit = async () => {
  let submitValue = addTeamData.value;
  let expireTime = null;
  if (submitValue.expireTime && submitValue.expireTime.length > 0) {
    expireTime = submitValue.expireTime.join("-");
  }
  const postData = {
    ...addTeamData.value,
    expireTime,
    status: Number(addTeamData.value.status)
  }
  const res = await myAxios.post("/team/update", postData);
  if (res?.code === 23200 && res.data) {
    showSuccessToast('更新成功');
    router.push({
      path: '/team',
      replace: true,
    });
  } else {
    showSuccessToast('更新失败');
  }
}
</script>

<style scoped>

</style>