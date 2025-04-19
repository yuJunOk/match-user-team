import myAxios from "../plugins/MyAxios.ts";
import {getCurrentUserStates, setCurrentUserStates} from "../states/user.ts";
import {showFailToast} from "vant";
import {useRouter} from "vue-router";

let router = useRouter();

export const getCurrentUser = async () => {
    const currentUser = getCurrentUserStates();
    if (!currentUser) {
        const res = await myAxios.get('user/current');
        if (res.code !== 23200){
            showFailToast('当前用户未登录！')
            router.replace('/user/login');
        }
        setCurrentUserStates(res.data);
        return res.data;
    }
    return currentUser;
}