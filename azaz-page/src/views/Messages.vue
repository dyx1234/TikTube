<script setup>
import { ref, onMounted, defineEmits } from 'vue';
import axios from 'axios';
import notice from '../components/notice.vue';
import qs from 'qs';
import { RouterLink } from 'vue-router';
import { NIcon, NResult, NAvatar, NThing } from 'naive-ui';

const emit = defineEmits(['wanted']);


axios.defaults.timeout = 5000;                         //请求超时时间
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';//请求头
axios.defaults.headers['Accept'] = '*/*';

const FollowNum = ref(-1);

const FollowData = ref([]);

async function getFollow()
{
    try 
    {
        // 发送请求
        const url = '/azaz/interact/message/chatList?';
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            FollowData.value = responseData.data.chatList;
            FollowNum.value = FollowData.value.length;
        } else
        {
            window.$message.error(
                responseData.message
            );
        }
    } catch (error)
    {
        // 网络请求失败
        window.$message.error(
            '网络异常。'
        );
    }
}

onMounted(() =>
{
    emit('wanted', 'checkLogin');      //应当保证登录
    const account = localStorage.getItem('Useraccount');
    if (account)
    {
        const userAccount = JSON.parse(localStorage.getItem('Useraccount'));
        axios.defaults.headers.common['token'] = userAccount.UserToken;//登陆后更新token
        getFollow();
    }

});
</script>

<template>
    <n-result title="暂无内容" description="去别处看看吧" size="huge" v-if="FollowNum == 0">
                            <template #icon>
                                <n-icon size="80" style="margin-top: 40px;"><svg xmlns="http://www.w3.org/2000/svg"
                                        xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 32 32">
                                        <path d="M9 10.555L10.555 9L23 21.444L21.444 23z" fill="currentColor"></path>
                                        <path
                                            d="M16 2A13.914 13.914 0 0 0 2 16a13.914 13.914 0 0 0 14 14a13.914 13.914 0 0 0 14-14A13.914 13.914 0 0 0 16 2zm0 26a12 12 0 1 1 12-12a12.035 12.035 0 0 1-12 12z"
                                            fill="currentColor"></path>
                                    </svg></n-icon>
                            </template>
                        </n-result>
    <div class="follow-grid" v-else>
        <RouterLink v-for="follow in FollowData" :key="follow.id" :to="'/user/' + follow.id">
            <n-thing content-indented>
                <template #avatar>
                    <n-avatar round :size="50" :src="follow.image" style="margin-right: 20px;" />
                </template>
                <template #header>
                    {{ follow.username }}
                </template>
                <template #description>
                    {{ follow.signature }}
                </template>
            </n-thing>
        </RouterLink>
    </div>
</template>

<style>
.follow-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
    grid-gap: 20px;
    margin: 0 10px 0 10px;
}
</style>
