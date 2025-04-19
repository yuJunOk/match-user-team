<template>
  <div
      id="teamCardList"
  >
    <van-card
        v-for="team in props.teamList"
        :desc="team.description"
        thumb="https://fastly.jsdelivr.net/npm/@vant/assets/leaf.jpeg"
        :title="`${team.name} `"
    >
      <template #tags>
        <van-tag plain type="danger" style="margin-right: 8px; margin-top: 8px" >
          {{
            teamStatusEnum[team.status]
          }}
        </van-tag>
      </template>
      <template #bottom>
        <div>
          {{`队伍人数：${team.hasJoinNum}/${team.maxNum}`}}
        </div>
        <div v-if="team.expireTime">
          {{'过期时间' + team.expireTime}}
        </div>
        <div>
          {{'创建时间' + team.createTime}}
        </div>
      </template>
      <template #footer>
        <van-button size="mini" plain type="primary" @click="preJoinTeam(team)">加入队伍</van-button>
        <van-button v-if="team.userId === currentUser?.id" size="mini" plain type="primary" @click="doUpdateTeam(team.id)">更新队伍</van-button>
        <van-button size="mini" plain type="danger" @click="doQuitTeam(team.id)">退出队伍</van-button>
        <van-button v-if="team.userId === currentUser?.id" size="mini" plain type="danger" @click="doDeleteTeam(team.id)">解散队伍</van-button>
      </template>
    </van-card>

    <van-dialog v-model:show="showPasswordDialog" title="请输入密码" show-cancel-button @confirm="doJoinTeam" @cancel="doJoinCancel">
      <van-field v-model="password" placeholder="请输入密码"/>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import {TeamType} from "../models/team";
import {teamStatusEnum} from "../constants/team";
import myAxios from "../plugins/myAxios";
import {Toast} from "vant";
import {onMounted, ref} from "vue";
import {getCurrentUser} from "../services/user.ts";
import {useRouter} from "vue-router";

interface TeamCardListProps{
  teamList: TeamType[];
}
const props = withDefaults(defineProps<TeamCardListProps>(),{
  //@ts-ignore
  teamList: [] as TeamType[],
});

const currentUser = ref();
const router = useRouter();

onMounted(async () => {
  currentUser.value = await getCurrentUser();
})

const showPasswordDialog = ref(false);
const password = ref('');
const joinTeamId = ref(0);

/**
 * 判断是不是加密房间，是的话显示密码框
 * @param team
 */
const preJoinTeam = (team: TeamType) => {
  joinTeamId.value = team.id;
  if (team.status === 0) {
    doJoinTeam()
  } else {
    showPasswordDialog.value = true;
  }
}

const doJoinCancel = () => {
  joinTeamId.value = 0;
  password.value = '';
}

/**
 * 队伍列表加入队伍
 * @param id
 */
const doJoinTeam = async() =>{
  if (!joinTeamId.value){
    return;
  }
  const res = await myAxios.post("/team/join",{
    teamId: joinTeamId.value,
    password: password.value
  });
  if (res?.code === 0){
    Toast.success("加入成功")
    doJoinCancel();
  }else {
    Toast.fail("加入失败" + (res.description ? `， ${res.description} `:''));
  }
}

/**
 * 跳转至更新队伍页
 */
const doUpdateTeam = async(id: number) =>{
  router.push({
    path: "/team/update",
    query: {
      id
    }
  });
}

/**
 * 退出队伍
 * @param id
 */
const doQuitTeam = async (id: number) => {
  const res = await myAxios.post('/team/quit', {
    teamId: id
  });
  if (res?.code === 23200) {
    Toast.success('操作成功');
  } else {
    Toast.fail('操作失败' + (res.message ? `，${res.message}` : ''));
  }
}

/**
 * 解散队伍
 * @param id
 */
const doDeleteTeam = async (id: number) => {
  const res = await myAxios.post('/team/delete', {
    id,
  });
  if (res?.code === 23200) {
    Toast.success('操作成功');
  } else {
    Toast.fail('操作失败' + (res.message ? `，${res.message}` : ''));
  }
}
</script>

<style scoped>
#teamCardList :deep(.van-image__img){
  height: 128px;
  object-fit: unset;
}
</style>