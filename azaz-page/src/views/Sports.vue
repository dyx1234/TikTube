<script setup>
import { watch, ref, onMounted, defineEmits } from 'vue';
import axios from 'axios';
import FlowCard from '../components/videocard.vue';
import qs from 'qs';
import { NDrawerContent, NDrawer, NEmpty, NInput, NAvatar, NThing, NSpin, NIcon, NButton } from 'naive-ui';

const emit = defineEmits(['wanted']);
const SECTION = 2;

axios.defaults.timeout = 10000;                         //请求超时时间
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';//请求头
axios.defaults.headers['Accept'] = '*/*';


const videoPlayer = ref(false);
const videoPlayerIndex = ref(0);

const VideoData = ref({
    VideoList: [],
    LastVideoId: 0,
});
const VideoNow = ref([]);
const getVideoing = ref(false);
const VideoComments = ref([]);
const VideoSeComments = ref([]);
const showSeComments = ref(false);
const commentText = ref('');
const parentCommentId = ref(0);
const watchParentCommentId = ref(0);

var FirstVideoId = -1;

watch(showSeComments, (newValue, oldValue) =>
{
    if (newValue)
    {
        getSeComment();
    }
});

async function getSeComment()
{
    try 
    {
        // 发送请求
        const url = '/azaz/video/getCommentList?' + qs.stringify({
            videoId: VideoNow.value.videoId,
            commentId: watchParentCommentId.value,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoSeComments.value = responseData.data;
        } else
        {
            window.$message.error(
                responseData.message
            );
        }
    } catch (error)
    {
    }
}

async function putComment()
{
    try 
    {
        // 发送请求
        const url = '/azaz/video/doComment?' + qs.stringify({
            parentId: parentCommentId.value,
            videoId: VideoNow.value.videoId,
            content: commentText.value,
        });
        const response = await axios.post(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            window.$message.success(
                '评论发布成功'
            );
            commentText.value = '';
            setTimeout(async () =>
            {
                getSeComment();
                try 
                {
                    // 发送请求
                    const url = '/azaz/video/getCommentList?' + qs.stringify({
                        videoId: VideoNow.value.videoId,
                        commentId: 0,
                    });
                    const response = await axios.get(url);

                    // 处理响应
                    const responseData = response.data;
                    if (responseData.code === 2)
                    {
                        VideoComments.value = responseData.data;
                    } else
                    {
                        window.$message.error(
                            responseData.message
                        );
                    }
                } catch (error)
                {
                }
            }, 1000);
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
            '网络异常：' + error
        );
    }
}

async function getVideoNow(Id)
{
    try 
    {
        // 发送请求
        const url = '/azaz/video/detailInfo?' + qs.stringify({
            videoId: Id,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoNow.value = responseData.data;
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
            '网络异常：' + error
        );
    }
    try 
    {
        // 发送请求
        const url = '/azaz/video/getCommentList?' + qs.stringify({
            videoId: Id,
            commentId: 0,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoComments.value = responseData.data;
        } else
        {
            window.$message.error(
                responseData.message
            );
        }
    } catch (error)
    {
    }
}

function showplay(index)
{
    videoPlayerIndex.value = index;
    getVideoNow(VideoData.value.VideoList[index].id);
    videoPlayer.value = true;
}

async function getVideo()
{
    getVideoing.value = true;
    try 
    {
        // 发送请求
        const url = '/azaz/video/getVideos?' + qs.stringify({
            lastVideoId: VideoData.value.LastVideoId,
            section: SECTION,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoData.value.LastVideoId = responseData.data.lastVideoId;
            VideoData.value.VideoList = VideoData.value.VideoList.concat(responseData.data.videoList);
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
            '网络异常：' + error
        );
    }
    getVideoing.value = false;
}

async function VideoLikeAction(action)
{
    try 
    {
        // 发送请求
        const url = '/azaz/video/doLike?' + qs.stringify({
            authorId: VideoNow.value.authorId,
            type: action,
            videoId: VideoNow.value.videoId,
        });//操作类型 1为点赞，0为取消点赞
        const response = await axios.post(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoNow.value.isLiked = !VideoNow.value.isLiked;
            if (action === 1)
            {
                VideoNow.value.likes++;
            } else
            {
                VideoNow.value.likes--;
            }
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
            '网络异常，请重试。'
        );
    }
}

async function VideoCollectAction(action)
{
    try 
    {
        // 发送请求
        const url = 'azaz/video/doCollect?' + qs.stringify({
            authorId: VideoNow.value.authorId,
            type: action,
            videoId: VideoNow.value.videoId,
        });//操作类型 1为收藏,0为取消收藏
        const response = await axios.post(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            VideoNow.value.isCollected = !VideoNow.value.isCollected;
            if (action === 1)
            {
                VideoNow.value.collects++;
            } else
            {
                VideoNow.value.collects--;
            }
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
            '网络异常，请重试。'
        );
    }
}

function videoChange(e)
{
    if (e.deltaY > 0)
    {
        // 向下滚动
        if (videoPlayerIndex.value < VideoData.value.VideoList.length)
        {
            if (videoPlayerIndex.value > VideoData.value.VideoList.length - 5)
            {
                getVideo();
            }
            videoPlayerIndex.value++;
            getVideoNow(VideoData.value.VideoList[videoPlayerIndex.value].id);
            watchParentCommentId.value = 0;
            parentCommentId.value = 0;
        }
    } else
    {
        // 向上滚动
        if (videoPlayerIndex.value > 0)
        {
            videoPlayerIndex.value--;
            getVideoNow(VideoData.value.VideoList[videoPlayerIndex.value].id);
            watchParentCommentId.value = 0;
            parentCommentId.value = 0;
        }
        else if (FirstVideoId != -1 && videoPlayerIndex.value === 0)
        {
            videoPlayerIndex.value--;
            getVideoNow(FirstVideoId);
            watchParentCommentId.value = 0;
            parentCommentId.value = 0;
        }
    }
}

function getQueryVariable()
{
    const url = window.location.href;
    const regex = /Vid=(\d+)/;
    const match = url.match(regex);

    if (match)
    {
        return match[1];
    } else
    {
        return null;
    }
}

function playstart(Vid)
{
    videoPlayerIndex.value = -1;
    getVideoNow(Vid);
    videoPlayer.value = true;
    FirstVideoId = Vid;
}

onMounted(() =>
{
    emit('wanted', 'checkLogin');      //应当保证登录
    const account = localStorage.getItem('Useraccount');
    if (account)
    {
        const userAccount = JSON.parse(localStorage.getItem('Useraccount'));
        axios.defaults.headers.common['token'] = userAccount.UserToken;//登陆后更新token
        const Vid = getQueryVariable();
        if (Vid)
        {
            playstart(Vid);
        }
        getVideo();
    }
});

function checkinput(v)
{
    if (commentText.value === "" && v.key === "Backspace")
    {
        parentCommentId.value = 0;
    }
}

function afterClose()
{
    parentCommentId.value = 0;
}
</script>

<template>
    <div class="flow-grid">
        <FlowCard v-for="(video, index) in VideoData.VideoList" :coverurl="video.coverUrl" :commentsnumber="video.comments"
            :title="video.title" :upname="video.userName" :likenumber="video.likes" :imgurl="video.image"
            :userid="video.authorId" :index="index" @showplay="showplay" />
        <div id="video-GetNext">
            <div v-if="getVideoing" class="video-Canva">
                <n-spin size="large" />
                <p>加载中</p>
            </div>
            <div v-else class="video-Canva">
                <n-button circle @click="getVideo" size="large">
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
                <p>获取新视频</p>
            </div>
        </div>
    </div>
    <div id="video-canva" v-if="videoPlayer">
        <div id="video-player-container">
            <div id="video-player-close">
                <n-button circle size="large" color="#505050" @click="videoPlayer = false">
                    <n-icon size="30"><svg color="white" xmlns="http://www.w3.org/2000/svg"
                            xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 512 512" style="">
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                stroke-width="32" d="M368 368L144 144" style=""></path>
                            <path fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                stroke-width="32" d="M368 144L144 368" style=""></path>
                        </svg></n-icon>
                </n-button>
            </div>
            <div id="video-player-frame" @wheel="videoChange">
                <video loop id="video-player-video" :src="VideoNow.videoUrl" controls="controls" autoplay="autoplay"
                    preload=""></video>
            </div>
        </div>
        <div id="video-detail-container">
            <div id="video-detail-thing">
                <n-thing style="margin-top: 30px;">
                    <template #avatar>
                        <n-avatar round :size="50" :src="VideoNow.image" style="margin-right: 10px;" />
                    </template>
                    <template #header>
                        <p style="font-weight: bold;font-size: 25px;margin: 0;padding-top: 4px;">{{ VideoNow.userName }}</p>
                    </template>
                </n-thing>
            </div>
            <div id="video-data-title">{{ VideoNow.title }}</div>
            <div id="video-data-show">
                <n-button circle strong secondary v-if="!VideoNow.isLiked" @click="VideoLikeAction(1);">
                    <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 24 24">
                                <g fill="none">
                                    <path
                                        d="M16.5 5.203c0-2.442-1.14-4.2-3.007-4.2c-1.026 0-1.378.602-1.746 2c-.075.29-.112.43-.151.569c-.101.358-.277.97-.527 1.83a.25.25 0 0 1-.03.065L8.174 9.953a5.885 5.885 0 0 1-2.855 2.327l-.473.181a2.75 2.75 0 0 0-1.716 3.092l.404 2.086a3.25 3.25 0 0 0 2.417 2.538l7.628 1.87a4.75 4.75 0 0 0 5.733-3.44l1.415-5.55a3.25 3.25 0 0 0-3.15-4.053h-1.822c.496-1.633.746-2.892.746-3.801zM4.6 15.267a1.25 1.25 0 0 1 .78-1.405l.474-.181a7.384 7.384 0 0 0 3.582-2.92l2.867-4.486c.09-.14.159-.294.205-.454c.252-.865.428-1.48.53-1.843c.044-.153.085-.309.159-.593c.19-.722.283-.881.295-.881c.868 0 1.507.984 1.507 2.699c0 .884-.326 2.335-.984 4.315a.75.75 0 0 0 .711.986h2.85a1.75 1.75 0 0 1 1.696 2.182l-1.415 5.55a3.25 3.25 0 0 1-3.923 2.353l-7.628-1.87a1.75 1.75 0 0 1-1.301-1.366L4.6 15.267z"
                                        fill="currentColor"></path>
                                </g>
                            </svg></n-icon>
                    </template>
                </n-button>
                <n-button circle strong secondary v-else @click="VideoLikeAction(0);">
                    <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 20 20">
                                <g fill="none">
                                    <path
                                        d="M12.483 1.704c-.799-.837-2.092-.387-2.431.59c-.28.806-.644 1.772-.998 2.483c-1.06 2.126-1.678 3.335-3.384 4.849a2.84 2.84 0 0 1-.841.49c-1.13.446-2.19 1.616-1.913 3.005l.353 1.765a2.5 2.5 0 0 0 1.794 1.922l5.6 1.527a4.5 4.5 0 0 0 5.61-3.537l.685-3.761A3 3 0 0 0 14.006 7.5h-.883l.01-.052c.08-.409.176-.97.24-1.583c.065-.61.1-1.285.049-1.913c-.05-.616-.184-1.249-.504-1.73a4.158 4.158 0 0 0-.435-.518z"
                                        fill="currentColor"></path>
                                </g>
                            </svg></n-icon>
                    </template>
                </n-button>
                <span class="video-data-number">{{ VideoNow.likes }}</span>
                <n-button circle strong secondary>
                    <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 28 28">
                                <g fill="none">
                                    <path
                                        d="M6 6.01v-.25a3.75 3.75 0 0 1 3.75-3.75h10.5A5.75 5.75 0 0 1 26 7.76v6.99a3.75 3.75 0 0 1-3.75 3.75H22v.25a3.75 3.75 0 0 1-3.75 3.75h-5.997l-4.245 3.235c-.823.627-2.008.04-2.008-.994V22.5h-.25A3.75 3.75 0 0 1 2 18.75V9.76a3.75 3.75 0 0 1 3.75-3.75H6zm1.5-.25v.25h10.75A3.75 3.75 0 0 1 22 9.76V17h.25a2.25 2.25 0 0 0 2.25-2.25V7.76a4.25 4.25 0 0 0-4.25-4.25H9.75A2.25 2.25 0 0 0 7.5 5.76zm-4 4v8.99A2.25 2.25 0 0 0 5.75 21h1a.75.75 0 0 1 .75.75v2.486l4.046-3.082A.75.75 0 0 1 12 21h6.25a2.25 2.25 0 0 0 2.25-2.25V9.76a2.25 2.25 0 0 0-2.25-2.25H5.75A2.25 2.25 0 0 0 3.5 9.76z"
                                        fill="currentColor"></path>
                                </g>
                            </svg></n-icon>
                    </template>
                </n-button>
                <span class="video-data-number">{{ VideoNow.comments }}</span>
                <n-button circle strong secondary v-if="!VideoNow.isCollected" @click="VideoCollectAction(1);">
                    <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 48 48">
                                <g fill="none">
                                    <path
                                        d="M21.803 6.085c.899-1.82 3.495-1.82 4.394 0l4.852 9.832l10.85 1.577c2.01.292 2.813 2.762 1.358 4.179l-7.85 7.653l1.853 10.806c.343 2.002-1.758 3.528-3.555 2.583L24 37.613l-9.705 5.102c-1.797.945-3.898-.581-3.555-2.583l1.854-10.806l-7.851-7.653c-1.455-1.417-.652-3.887 1.357-4.179l10.85-1.577l4.853-9.832zM24 7.283l-4.82 9.764a2.45 2.45 0 0 1-1.844 1.34L6.56 19.954l7.798 7.601a2.45 2.45 0 0 1 .704 2.169l-1.84 10.732l9.638-5.067a2.45 2.45 0 0 1 2.28 0l9.638 5.067l-1.84-10.732a2.45 2.45 0 0 1 .704-2.169l7.798-7.6l-10.776-1.566a2.45 2.45 0 0 1-1.845-1.34L24 7.282z"
                                        fill="currentColor"></path>
                                </g>
                            </svg></n-icon>
                    </template>
                </n-button>
                <n-button circle strong secondary v-else @click="VideoCollectAction(0);">
                    <template #icon>
                        <n-icon><svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 48 48">
                                <g fill="none">
                                    <path
                                        d="M21.803 6.085c.899-1.82 3.495-1.82 4.394 0l4.852 9.832l10.85 1.577c2.01.292 2.813 2.762 1.358 4.179l-7.85 7.653l1.853 10.806c.343 2.002-1.758 3.528-3.555 2.583L24 37.613l-9.705 5.102c-1.797.945-3.898-.581-3.555-2.583l1.854-10.806l-7.851-7.653c-1.455-1.417-.652-3.887 1.357-4.179l10.85-1.577l4.853-9.832z"
                                        fill="currentColor"></path>
                                </g>
                            </svg></n-icon>
                    </template>
                </n-button>
                <span class="video-data-number">{{ VideoNow.collects }}</span>
            </div>
            <div id="video-comments">
                <div id="video-comments-data">
                    <n-empty description="暂无评论" v-if="VideoComments.length < 1" style="margin-top: 50px;"></n-empty>
                    <n-thing style="margin-top: 15px;" v-for="comment in VideoComments" content-indented v-else>
                        <template #avatar>
                            <n-avatar round :size="35" :src="comment.image" style="margin-right: 4px;" />
                        </template>
                        <template #header>
                            <p style="font-weight: bold;font-size: 20px;margin: 0;padding-top: 3px;">{{ comment.userName }}
                            </p>
                        </template>
                        <template #action>
                            <n-button text style="color: #87888e;"
                                @click="{ showSeComments = true; watchParentCommentId = comment.id; parentCommentId = comment.id; }">
                                展开回复
                            </n-button>
                            <n-button text style="margin-left: 20px;color: #87888e;" @click="parentCommentId = comment.id">
                                回复
                            </n-button>
                        </template>
                        {{ comment.content }}
                    </n-thing>
                </div>
                <n-drawer v-model:show="showSeComments" resizable default-height="500" placement="top" :trap-focus="false"
                    :block-scroll="false" to="#video-comments-data" style="margin-top: 30px;" :on-after-leave="afterClose">
                    <n-drawer-content title="查看回复" closable footer-style="display: block;">
                        <n-empty description="暂无评论" v-if="VideoSeComments.length < 1" style="margin-top: 50px;"></n-empty>
                        <n-thing style="margin-top: 15px;" v-for="comment in VideoSeComments" content-indented v-else>
                            <template #avatar>
                                <n-avatar round :size="35" :src="comment.image" style="margin-right: 4px;" />
                            </template>
                            <template #header>
                                <p style="font-weight: bold;font-size: 20px;margin: 0;padding-top: 3px;">{{ comment.userName
                                }}
                                </p>
                            </template>
                            <template #action>
                                <n-button text style="color: #87888e;"
                                    @click="{ watchParentCommentId = comment.id; getSeComment();parentCommentId = comment.id; }">
                                    展开回复
                                </n-button>
                                <n-button text style="margin-left: 20px;color: #87888e;"
                                    @click="parentCommentId = comment.id">
                                    回复
                                </n-button>
                            </template>
                            {{ comment.content }}
                        </n-thing>
                        <template #footer>
                            <n-input :autosize="{ minRows: 3, maxRows: 4 }" show-count :maxlength="120"
                                :count-graphemes="countGraphemes" v-model:value="commentText" type="textarea"
                                placeholder="此处期待一条评论" @keyup="checkinput">
                                <template #prefix v-if="parentCommentId != 0">
                                    回复
                                </template></n-input>
                            <div style="display: flex;justify-content: flex-end;"><n-button @click="putComment"
                                    style="height: 40px;width: 90px;margin-top: 20px;">
                                    <p class="edit-p">发布</p>
                                </n-button></div>
                        </template>
                    </n-drawer-content>
                </n-drawer>
                <div id="video-comments-input">
                    <n-input :autosize="{ minRows: 3, maxRows: 4 }" show-count :maxlength="120"
                        :count-graphemes="countGraphemes" v-model:value="commentText" type="textarea" placeholder="此处期待一条评论"
                        @keyup="checkinput">
                        <template #prefix v-if="parentCommentId != 0">
                            回复
                        </template></n-input>
                    <div style="display: flex;justify-content: flex-end;"><n-button @click="putComment"
                            style="height: 40px;width: 90px;margin-top: 20px;">
                            <p class="edit-p">发布</p>
                        </n-button></div>
                </div>
            </div>
        </div>
    </div>
</template>

<style>
#video-comments {
    flex: 1;
    display: flex;
    overflow: auto;
    flex-direction: column;
}

#video-comments-data {
    overflow: auto;
    flex: 1;
}

#video-comments-input {
    margin-top: 20px;
    height: 160px;
}

#video-data-title {
    font-weight: bold;
    font-size: 20px;
}

.video-data-number {
    font-weight: bold;
    font-size: 16px;
    margin-left: 10px;
    margin-right: 20px;
}

#video-data-show {
    margin-top: 15px;
    display: flex;
    align-items: center;
    margin-bottom: 20px;
}

#video-player-frame {
    width: 100%;
    height: 100%;
}

#video-player-video {
    width: 100%;
    height: 100%;
}

#video-player-close {
    position: absolute;
    top: 20px;
    left: 20px;
}

#video-player-container {
    position: relative;
    width: 68vw;
    height: 100%;
    background-color: black;
    display: flex;
    align-items: center;
    justify-content: center;
}

#video-detail-container {
    position: relative;
    width: 32vw;
    height: 100%;
    background-color: white;
    padding: 30px;
    display: flex;
    flex-direction: column;
}

#video-canva {
    height: 100vh;
    width: 100vw;
    position: fixed;
    top: 0px;
    left: 0px;
    z-index: 30;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.flow-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
    grid-gap: 20px;
    margin: 0 10px 0 10px;
}

#video-GetNext {
    height: 348px;
    width: 201px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.video-Canva {
    display: flex;
    flex-direction: column;
    align-items: center;
}
</style>
