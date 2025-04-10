<script setup>
import { ref, onMounted, defineEmits, watch } from 'vue';
import { useRoute, RouterLink } from 'vue-router';
import axios from 'axios';
import FlowCard from '../components/videocard.vue';
import notice from '../components/notice.vue';
import qs from 'qs';
import { NMessageProvider, NNotificationProvider, NAvatar, NThing, NTabs, NTabPane, NResult, NIcon, NButton, NSpin } from 'naive-ui';

const emit = defineEmits(['wanted']);

const route = useRoute();
const key = ref('');
const searchUsering = ref(false);
const searchUserEnd = ref(false);
const searchUserData = ref({
    Total: 0,
    userList: []
});
var searchUserPage = 1;
const searchVideoing = ref(false);
const searchVideoEnd = ref(false);
const searchVideoData = ref({
    Total: 0,
    VideoList: []
});
var searchVideoPage = 1;

watch(
    () => route.query.key,
    async newKey =>
    {
        key.value = newKey;
        searchUserPage = 1;
        searchVideoPage = 1;
        searchUserEnd.value = false;
        searchVideoEnd.value = false;
        searchUsering.value = false;
        searchVideoing.value = false;
        searchUserData.value = {
            Total: 0,
            userList: []
        };
        searchVideoData.value = {
            Total: 0,
            VideoList: []
        };
        searchUser();
        searchVideo();
    }
);

function showplay(index)
{
    window.open('#/?Vid=' + searchVideoData.value.VideoList[index].videoId, "_blank");
}

async function searchVideo()
{
    searchVideoing.value = true;
    try 
    {
        // 发送请求
        const url = '/azaz/search/video?' + qs.stringify({
            keyword: key.value,
            page: searchUserPage,
            pageSize: 10
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            searchVideoData.value.Total = responseData.data.total;
            searchVideoData.value.VideoList = searchVideoData.value.VideoList.concat(responseData.data.videoList);
            if (searchVideoData.value.VideoList.length >= searchVideoData.value.Total)
            {
                searchVideoEnd.value = true;
            }
            searchVideoPage = searchVideoPage + 1;
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
    searchVideoing.value = false;
}

async function searchUser()
{
    searchUsering.value = true;
    try 
    {
        // 发送请求
        const url = '/azaz/search/user?' + qs.stringify({
            keyword: key.value,
            page: searchUserPage,
            pageSize: 10
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            searchUserData.value.Total = responseData.data.total;
            searchUserData.value.userList = searchUserData.value.userList.concat(responseData.data.list);
            if (searchUserData.value.userList.length >= searchUserData.value.Total)
            {
                searchUserEnd.value = true;
            }
            searchUserPage = searchUserPage + 1;
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
    searchUsering.value = false;
}

onMounted(() =>
{
    key.value = route.query.key;
    emit('wanted', 'checkLogin');      //应当保证登录
    const account = localStorage.getItem('Useraccount');
    if (!account)
    {
        return;
    }
    const userAccount = JSON.parse(localStorage.getItem('Useraccount'));
    axios.defaults.headers.common['token'] = userAccount.UserToken;//登陆后更新token
    searchUser();
    searchVideo();
});
</script>

<template>
    <n-message-provider><n-notification-provider>
            <notice />
        </n-notification-provider></n-message-provider>
    <n-tabs justify-content="space-evenly" type="line">
        <n-tab-pane name="Video" tab="视频">
            <n-result title="暂无内容" description="去别处看看吧" size="huge" v-if="searchVideoEnd && searchVideoData.Total === 0">
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
            <div class="user-flow-grid" v-else>
                <FlowCard v-for="(video, index) in searchVideoData.VideoList" :coverurl="video.coverUrl"
                    :commentsnumber="video.comments" :title="video.title" :upname="video.userName" :likenumber="video.likes"
                    :imgurl="video.image" :userid="video.authorId" :index="index" @showplay="showplay" />
                <div id="user-GetNext" v-show="!searchUserEnd">
                    <div v-if="searchVideoing" class="user-Canva">
                        <n-spin size="large" />
                        <p>加载中</p>
                    </div>
                    <div v-else class="user-Canva">
                        <n-button circle @click="searchVideo" size="large">
                            <template #icon>
                                <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                        viewBox="0 0 20 20">
                                        <g fill="none">
                                            <path
                                                d="M13.5 5a.5.5 0 0 1 .492.41L14 5.5v9a.5.5 0 0 1-.992.09L13 14.5v-9a.5.5 0 0 1 .5-.5zm-7.854.146a.5.5 0 0 1 .638-.057l.07.057l4.5 4.5a.5.5 0 0 1 .057.638l-.057.07l-4.5 4.5a.5.5 0 0 1-.765-.638l.057-.07L9.793 10L5.646 5.854a.5.5 0 0 1 0-.708z"
                                                fill="currentColor"></path>
                                        </g>
                                    </svg></n-icon>
                            </template>
                        </n-button>
                        <p>获取更多</p>
                    </div>
                </div>
            </div>
        </n-tab-pane>
        <n-tab-pane name="User" tab="用户">
            <n-result title="暂无内容" description="去别处看看吧" size="huge" v-if="searchUserEnd && searchUserData.Total === 0">
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

                <RouterLink v-for="user in searchUserData.userList" :key="user.id" :to="'/user/' + user.id">
                    <n-thing content-indented>
                        <template #avatar>
                            <n-avatar round :size="50" :src="user.image" style="margin-right: 20px;" />
                        </template>
                        <template #header>
                            {{ user.username }}
                        </template>
                        <template #description>
                            {{ user.signature }}
                        </template>
                    </n-thing>
                </RouterLink>

                <div v-show="!searchUserEnd">
                    <div v-if="searchUsering" class="user-search">
                        <n-spin size="large" />
                        <p>加载中</p>
                    </div>
                    <div v-else class="user-search">
                        <n-button circle @click="searchUser" size="large">
                            <template #icon>
                                <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                        viewBox="0 0 20 20">
                                        <g fill="none">
                                            <path
                                                d="M13.5 5a.5.5 0 0 1 .492.41L14 5.5v9a.5.5 0 0 1-.992.09L13 14.5v-9a.5.5 0 0 1 .5-.5zm-7.854.146a.5.5 0 0 1 .638-.057l.07.057l4.5 4.5a.5.5 0 0 1 .057.638l-.057.07l-4.5 4.5a.5.5 0 0 1-.765-.638l.057-.07L9.793 10L5.646 5.854a.5.5 0 0 1 0-.708z"
                                                fill="currentColor"></path>
                                        </g>
                                    </svg></n-icon>
                            </template>
                        </n-button>
                        <p>获取更多</p>
                    </div>
                </div>

            </div>
        </n-tab-pane>
    </n-tabs>
</template>

<style>
.user-search {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
}
</style>