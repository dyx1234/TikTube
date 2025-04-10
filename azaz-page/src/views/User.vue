<script setup>
import { ref, onMounted, defineEmits, defineProps, watch } from 'vue';
import { NSpin, NDrawerContent, NDrawer, NUploadDragger, NUpload, NStatistic, NCol, NRow, NInput, NListItem, NList, NModal, NResult, NSkeleton, NTabPane, NTabs, NNotificationProvider, NMessageProvider, NAvatar, NThing, NButton, NIcon } from 'naive-ui';
import axios from 'axios';
import TinyCard from '../components/tinycard.vue';
import notice from '../components/notice.vue';
import qs from 'qs';

const props = defineProps(['id']);
const emit = defineEmits(['wanted']);


axios.defaults.timeout = 5000;                         //请求超时时间
axios.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';//请求头
axios.defaults.headers['Accept'] = '*/*';



const editInfo = ref(false);
const Userinfo = ref({
    UserImg: null,
    UserName: null,
    UserSignature: null,
    UserFans: null,
    UserFollow: null,
    UserWork: null,
    UserLiked: null,
    UserIsFollowed: false,
});
const Editinfo = ref({
    UserName: null,
    UserSignature: null,
});
const Userno = ref({
    UserToken: null,
    UserId: null,
});
const PublishData = ref({
    VideoList: [],
    Total: 0,
});
const CollectData = ref({
    VideoList: [],
    Total: 0,
});
const MessageData = ref([]);
const messageText = ref('');
const messageEnd = ref(false);
const getPublishing = ref(false);
const getPublishEnd = ref(false);
const getCollecting = ref(false);
const getCollectEnd = ref(false);
const IsSelf = ref(false);
const uploadCover = ref(null);
const isUping = ref(false);
const isValidate = ref(false);
const showMessage = ref(false);
var CoverUrl = "";
var isCoverUploading = false;
var fileNumber = 0;
var PublishPage = 1;
var CollectPage = 1;
var lastMessageId = 0;

watch(showMessage, (newValue, oldValue) =>
{
    if (newValue)
    {
        getMessage();
    }
});

async function putMessage()
{
    try 
    {
        // 发送请求
        const url = '/azaz/interact/message/send?' + qs.stringify({
            receiverId: props.id,
            content: messageText.value,
            status: 0,
        });
        const response = await axios.post(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            window.$message.success(
                '私信发布成功'
            );
            MessageData.value = MessageData.value.concat({
                senderId: Userno.value.UserId,
                messageContent: messageText.value,
                messageId: 0,
                status: 0,
            });
            messageText.value = '';
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

async function getMessage()
{
    if (messageEnd.value)
    {
        return;
    }
    try 
    {
        // 发送请求
        const url = '/azaz/interact/message/list?' + qs.stringify({
            friendId: props.id,
            lastMessageId: lastMessageId,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            if (!responseData.data.total)
            {
                messageEnd.value = true;
                window.$message.success(
                    '没有更早的消息'
                );
                return;
            }
            const reversedMessages = responseData.data.messages.slice().reverse();
            MessageData.value = reversedMessages.concat(MessageData.value);

            lastMessageId = responseData.data.lastMessageId;

        } else
        {
            window.$message.error(
                responseData.message
            );
        }
    } catch (error)
    {
        window.$message.error(
            '网络异常，请重试。'
        );
    }
}

const coverUpload = async ({ file, onFinish, onError, onProgress }) =>
{
    const formData = new FormData();
    formData.append('imageFile', file.file);

    try
    {
        isCoverUploading = true;
        const response = await axios.post('/azaz/user/image/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }, onUploadProgress: (progressEvent) =>
            {
                const percent = (progressEvent.loaded / progressEvent.total) * 100;
                onProgress({ percent: Math.ceil(percent) });
            },
            timeout: 120000
        });

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            CoverUrl = responseData.data;
            onFinish();
        } else
        {
            window.$message.error(
                responseData.message
            );
            onError();
        }
        isCoverUploading = false;
    } catch (error)
    {
        // 网络请求失败
        window.$message.error(
            '网络异常，请稍后重试。'
        );
        onError();
        isCoverUploading = false;
    }
};

async function submitedit()
{
    try 
    {
        // 发送请求
        const editurl = '/azaz/user/personal?' + qs.stringify({
            image: CoverUrl,
            username: Editinfo.value.UserName,
            signature: Editinfo.value.UserSignature,
        });
        const response = await axios.put(editurl);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            // 成功
            window.$message.success(
                '信息修改成功'
            );
            Userinfo.value.UserImg = CoverUrl;
            Userinfo.value.UserName = Editinfo.value.UserName;
            Userinfo.value.UserSignature = Editinfo.value.UserSignature;
            localStorage.setItem("Userdata", JSON.stringify({
                UserName: Userinfo.value.UserName,
                UserSignature: Userinfo.value.UserSignature,
                UserImg: Userinfo.value.UserImg,
            }));
            Editinfo.value = false;
            location.reload(true);
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
            '信息修改失败，网络异常，请稍后重试。'
        );
    }
}

// 定义修改信息函数
async function editinfo()
{

    isUping.value = true;
    uploadCover.value?.submit();
    const intervalId = setInterval(() =>
    {
        if (!isCoverUploading)
        {
            submitedit();
            isUping.value = false;
            clearInterval(intervalId);
        }
    }, 1000);

}

function fileChange(options)
{
    fileNumber = options.fileList.length;
    if ((fileNumber !== 1 && CoverUrl === "") || !Editinfo.value.UserName || !Editinfo.value.UserSignature)
    {
        isValidate.value = false;
    }
    else
    {
        isValidate.value = true;
    }
}

function checkVideo()
{
    if ((fileNumber !== 1 && CoverUrl === "") || !Editinfo.value.UserName || !Editinfo.value.UserSignature)
    {
        isValidate.value = false;
    }
    else
    {
        isValidate.value = true;
    }
}

function share()
{
    const currentURL = window.location.href.split('?')[0];
    if (navigator.clipboard)
    {
        navigator.clipboard.writeText(currentURL).then(() =>
        {
            window.$message.success(
                '成功复制分享链接'
            );
        }).catch(err =>
        {
            window.$message.error(
                '复制出错，请手动复制页面链接进行分享'
            );
        });
    } else
    {
        // 处理不支持Clipboard API 的情况
        window.$message.error(
            '当前浏览器不支持自动复制，请手动复制页面链接进行分享'
        );
    }
}

async function exit()
{
    try 
    {
        // 发送请求
        const url = '/azaz/user/login/logout?' + qs.stringify({
        });
        const response = await axios.delete(url);
    } catch (error)
    {
        
    }
    localStorage.removeItem('Useraccount');
    localStorage.removeItem('Userdata');
    window.$message.success(
        '成功退出登录'
    );
    setTimeout(() => { window.location.href = '#/'; }, 2000);
}

async function getPublished()
{
    getPublishing.value = true;
    try 
    {
        // 发送请求
        const url = '/azaz/video/getPublishedVideos?' + qs.stringify({
            currentPage: PublishPage,
            userId: props.id,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            PublishData.value.Total = responseData.data.total;
            PublishData.value.VideoList = PublishData.value.VideoList.concat(responseData.data.videoList);
            if (PublishData.value.VideoList.length >= PublishData.value.Total)
            {
                getPublishEnd.value = true;
            }
            PublishPage = PublishPage + 1;
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
    getPublishing.value = false;
}

async function getCollect()
{
    getCollecting.value = true;
    try 
    {
        // 发送请求
        const url = '/azaz/video/showCollectList?' + qs.stringify({
            currentPage: CollectPage,
            userId: props.id,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            CollectData.value.Total = responseData.data.total;
            CollectData.value.VideoList = CollectData.value.VideoList.concat(responseData.data.videoList);
            if (CollectData.value.VideoList.length >= CollectData.value.Total)
            {
                getCollectEnd.value = true;
            }
            CollectPage = CollectPage + 1;
            console.log(CollectData.value.VideoList);
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
    getCollecting.value = false;
}

onMounted(async () =>
{
    emit('wanted', 'checkLogin');      //应当保证登录
    const account = localStorage.getItem('Useraccount');
    if (!account)
    {
        return;
    }
    const userAccount = JSON.parse(localStorage.getItem('Useraccount'));
    axios.defaults.headers.common['token'] = userAccount.UserToken;//登陆后更新token
    Userno.value.UserToken = userAccount.UserToken;
    Userno.value.UserId = userAccount.UserId;
    try 
    {
        // 发送请求
        const url = '/azaz/user/homePage?' + qs.stringify({
            userId: props.id,
        });
        const response = await axios.get(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            // 成功
            Userinfo.value.UserName = responseData.data.username;
            Userinfo.value.UserImg = responseData.data.image;
            Userinfo.value.UserSignature = responseData.data.signature;
            Userinfo.value.UserFans = responseData.data.fansNum;  //被关注
            Userinfo.value.UserFollow = responseData.data.followNum;    //关注别人
            Userinfo.value.UserWork = responseData.data.workNum;
            Userinfo.value.UserLiked = responseData.data.likedNum;
            Userinfo.value.UserIsFollowed = responseData.data.isFollow;
            Editinfo.value.UserName = responseData.data.username;
            Editinfo.value.UserSignature = responseData.data.signature;
            CoverUrl = responseData.data.image;
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
            '网络异常，请刷新网页。'
        );
    }
    getPublished();
    if (props.id === userAccount.UserId)
    {
        IsSelf.value = true;
        getCollect();
    } else
    {
        IsSelf.value = false;
    }

});

async function FollowAction(action)
{
    try 
    {
        // 发送请求
        const url = '/azaz/interact/follow/do?' + qs.stringify({
            userId: props.id,
            type: action,
        });//操作类型 1-关注 0-取消关注
        const response = await axios.post(url);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            Userinfo.value.UserIsFollowed = !Userinfo.value.UserIsFollowed;
            if (action === 1)
            {
                Userinfo.value.UserFans++;
            } else
            {
                Userinfo.value.UserFans--;
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

async function beforeCoverUpload(data)
{
    if (data.file?.file.size > 1024 * 1024 * 5)
    {
        window.$message.error(
            '文件大小不应超过5M'
        );
        return false;
    }
    else if (!data.file?.type.startsWith("image/"))
    {
        window.$message.error(
            '封面必须为支持的图片文件类型。'
        );
        return false;
    }
    return true;
}
</script>

<template>
    <n-drawer v-model:show="showMessage" default-width="502" placement="right" resizable>
        <n-drawer-content title="私信内容" closable footer-style="display:block;">
            <n-list hoverable>
                <n-list-item v-if="!messageEnd">
                    <n-button strong secondary style="width: 100%;" @click="getMessage">
                        更早消息
                    </n-button>
                </n-list-item>
                <n-list-item v-for="message in MessageData">
                    <p :class="{ 'message-self': message.senderId === Userno.UserId }" class="message-p">{{
                        message.messageContent }}</p>
                </n-list-item>
            </n-list>
            <template #footer>
                <n-input :autosize="{ minRows: 3, maxRows: 4 }" show-count :maxlength="120"
                    :count-graphemes="countGraphemes" v-model:value="messageText" type="textarea"
                    placeholder="来聊天吧"></n-input>
                <div style="display: flex;justify-content: flex-end;"><n-button @click="putMessage"
                        style="height: 40px;width: 90px;margin-top: 20px;">
                        <p class="edit-p">发布</p>
                    </n-button></div>
            </template>
        </n-drawer-content>
    </n-drawer>
    <n-message-provider><n-notification-provider>
            <notice />
        </n-notification-provider></n-message-provider>
    <n-modal v-model:show="editInfo" preset="card" style="max-width: 500px;" id="edit-info" title="修改个人信息" size="huge"
        :bordered="false">

        <n-list hoverable clickable>
            <n-list-item>
                <n-thing content-style="margin-top: 10px;" content-indented>
                    <template #avatar>
                        <span class="edit-info-title">头像</span>
                    </template>
                    <template #description>
                        <n-upload @change="fileChange" :show-cancel-button="!isUping" :disabled="isUping"
                            :show-download-button="false" @before-upload="beforeCoverUpload" :show-remove-button="false"
                            :custom-request="coverUpload" :default-upload="false" max="1" ref="uploadCover">
                            <n-upload-dragger
                                style="height: 100px;display: flex;align-items: center;justify-content: center;">点击或拖动来上传头像</n-upload-dragger>
                        </n-upload>
                    </template>
                </n-thing>
            </n-list-item>
            <n-list-item>
                <n-thing content-style="margin-top: 10px;" content-indented>
                    <template #avatar>
                        <span class="edit-info-title">昵称</span>
                    </template>
                    <template #description>
                        <n-input @input="checkVideo" :disabled="isUping" v-model:value="Editinfo.UserName" type="text"
                            :placeholder="Userinfo.UserName" show-count :maxlength="20" :count-graphemes="countGraphemes" />
                    </template>
                    <template #footer>
                        昵称只能包含字母、数字、下划线和句点。不超过20个字符。
                    </template>
                </n-thing>
            </n-list-item>
            <n-list-item>
                <n-thing content-style="margin-top: 10px;" content-indented>
                    <template #avatar>
                        <span class="edit-info-title">个性签名</span>
                    </template>
                    <template #description>
                        <n-input @input="checkVideo" :disabled="isUping" v-model:value="Editinfo.UserSignature"
                            type="textarea" :placeholder="Userinfo.UserSignature" show-count :maxlength="100"
                            :count-graphemes="countGraphemes" />
                    </template>
                    <template #footer>
                        请输入个性签名，不超过100个字符。
                    </template>
                </n-thing>
            </n-list-item>
        </n-list>
        <div class="edit-btn"><n-button strong secondary @click="editinfo" class="edit-button-sub"
                :disabled="!isValidate || isUping">
                <n-icon size="20">
                    <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 32 32">
                        <path
                            d="M27.307 6.107L30 3.414L28.586 2l-2.693 2.693L24.8 3.6a1.933 1.933 0 0 0-2.8 0l-18 18V28h6.4l18-18a1.933 1.933 0 0 0 0-2.8zM9.6 26H6v-3.6L23.4 5L27 8.6z"
                            fill="currentColor"></path>
                        <path d="M9 11.586L16.586 4L18 5.414L10.414 13z" fill="currentColor"></path>
                    </svg>
                </n-icon>
                <p class="edit-p">提交</p>
            </n-button></div>
    </n-modal>
    <div>
        <n-thing>
            <template #avatar>
                <n-avatar round :size="150" :src="Userinfo.UserImg" style="margin-right: 20px;" />
            </template>
            <template #header>
                <p id="user-name">{{ Userinfo.UserName }}</p>
            </template>
            <template #header-extra>
                <n-button circle size="large" @click="share">
                    <template #icon>
                        <n-icon size="25">
                            <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                viewBox="0 0 24 24">
                                <g fill="none">
                                    <path
                                        d="M6.747 4h3.464a.75.75 0 0 1 .102 1.493l-.102.007H6.747a2.25 2.25 0 0 0-2.245 2.095l-.005.155v9.5a2.25 2.25 0 0 0 2.096 2.244l.154.006h9.5a2.25 2.25 0 0 0 2.245-2.096l.005-.154v-.498a.75.75 0 0 1 1.494-.102l.006.102v.498a3.75 3.75 0 0 1-3.55 3.744l-.2.006h-9.5a3.75 3.75 0 0 1-3.745-3.551l-.005-.2v-9.5a3.75 3.75 0 0 1 3.55-3.744l.2-.005h3.464h-3.464zM14.5 6.52V3.75a.75.75 0 0 1 1.187-.61l.082.068l5.994 5.75c.28.269.306.7.077.998l-.077.085l-5.994 5.752a.75.75 0 0 1-1.262-.435l-.007-.107v-2.725l-.344.03c-2.4.25-4.7 1.331-6.914 3.26c-.52.453-1.323.025-1.237-.658c.664-5.32 3.446-8.251 8.195-8.619l.3-.02V3.75v2.77zM16 5.507V7.25a.75.75 0 0 1-.75.75c-3.874 0-6.274 1.676-7.312 5.157l-.079.278l.352-.237C10.45 11.737 12.798 11 15.251 11a.75.75 0 0 1 .743.649l.007.101v1.743L20.16 9.5l-4.16-3.992z"
                                        fill="currentColor"></path>
                                </g>
                            </svg>
                        </n-icon>
                    </template>
                </n-button>
            </template>
            <template #description class="edit-container" v-if="IsSelf">
                <n-button @click="editInfo = true" class="edit-button">
                    <n-icon size="20">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 32 32">
                            <path
                                d="M27.307 6.107L30 3.414L28.586 2l-2.693 2.693L24.8 3.6a1.933 1.933 0 0 0-2.8 0l-18 18V28h6.4l18-18a1.933 1.933 0 0 0 0-2.8zM9.6 26H6v-3.6L23.4 5L27 8.6z"
                                fill="currentColor"></path>
                            <path d="M9 11.586L16.586 4L18 5.414L10.414 13z" fill="currentColor"></path>
                        </svg>
                    </n-icon>
                    <p class="edit-p">修改个人信息</p>
                </n-button>
                <n-button @click="exit" class="edit-button">
                    <n-icon size="20">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 32 32">
                            <path d="M26 4h2v24h-2z" fill="currentColor"></path>
                            <path d="M11.414 20.586L7.828 17H22v-2H7.828l3.586-3.586L10 10l-6 6l6 6l1.414-1.414z"
                                fill="currentColor"></path>
                        </svg>
                    </n-icon>
                    <p class="edit-p">安全退出登录</p>
                </n-button>
            </template>
            <template #description class="edit-container" v-else>
                <n-button strong secondary @click="FollowAction(0);" class="edit-button" v-if="Userinfo.UserIsFollowed">
                    <n-icon size="22">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 24 24">
                            <path d="M17 11v6.97l-5-2.14l-5 2.14V5h6V3H7c-1.1 0-2 .9-2 2v16l7-3l7 3V11h-2zm4-4h-6V5h6v2z"
                                fill="currentColor"></path>
                        </svg>
                    </n-icon>
                    <p class="edit-p">取消关注</p>
                </n-button>
                <n-button @click="FollowAction(1);" class="edit-button" v-else>
                    <n-icon size="20">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 32 32">
                            <path
                                d="M24 16v10.752l-7.096-3.59l-.904-.457l-.9.456L8 26.748V4h10V2H8a2 2 0 0 0-2 2v26l10-5.054L26 30V16z"
                                fill="currentColor"></path>
                            <path d="M26 6V2h-2v4h-4v2h4v4h2V8h4V6h-4z" fill="currentColor"></path>
                        </svg>
                    </n-icon>
                    <p class="edit-p">关注账号</p>
                </n-button>
                <n-button @click="{showMessage = true;}" class="edit-button">
                    <n-icon size="20">
                        <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                            viewBox="0 0 1024 1024">
                            <path
                                d="M464 512a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm200 0a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm-400 0a48 48 0 1 0 96 0a48 48 0 1 0-96 0zm661.2-173.6c-22.6-53.7-55-101.9-96.3-143.3a444.35 444.35 0 0 0-143.3-96.3C630.6 75.7 572.2 64 512 64h-2c-60.6.3-119.3 12.3-174.5 35.9a445.35 445.35 0 0 0-142 96.5c-40.9 41.3-73 89.3-95.2 142.8c-23 55.4-34.6 114.3-34.3 174.9A449.4 449.4 0 0 0 112 714v152a46 46 0 0 0 46 46h152.1A449.4 449.4 0 0 0 510 960h2.1c59.9 0 118-11.6 172.7-34.3a444.48 444.48 0 0 0 142.8-95.2c41.3-40.9 73.8-88.7 96.5-142c23.6-55.2 35.6-113.9 35.9-174.5c.3-60.9-11.5-120-34.8-175.6zm-151.1 438C704 845.8 611 884 512 884h-1.7c-60.3-.3-120.2-15.3-173.1-43.5l-8.4-4.5H188V695.2l-4.5-8.4C155.3 633.9 140.3 574 140 513.7c-.4-99.7 37.7-193.3 107.6-263.8c69.8-70.5 163.1-109.5 262.8-109.9h1.7c50 0 98.5 9.7 144.2 28.9c44.6 18.7 84.6 45.6 119 80c34.3 34.3 61.3 74.4 80 119c19.4 46.2 29.1 95.2 28.9 145.8c-.6 99.6-39.7 192.9-110.1 262.7z"
                                fill="currentColor"></path>
                        </svg>
                    </n-icon>
                    <p class="edit-p">发送私信</p>
                </n-button>
            </template>
            <n-row>
                <n-col :span="3">
                    <n-statistic label="粉丝数">
                        {{ Userinfo.UserFans }}
                    </n-statistic>
                </n-col>
                <n-col :span="3">
                    <n-statistic label="获赞数">
                        {{ Userinfo.UserLiked }}
                    </n-statistic>
                </n-col><n-col :span="3">
                    <n-statistic label="作品数">
                        {{ Userinfo.UserWork }}
                    </n-statistic>
                </n-col><n-col :span="3">
                    <n-statistic label="关注数">
                        {{ Userinfo.UserFollow }}
                    </n-statistic>
                </n-col>
            </n-row>
            <template #footer>
                <p id="user-sig">{{ Userinfo.UserSignature }}</p>
            </template>
        </n-thing>
        <div id="user-flow">
            <n-tabs type="line" animated size="large"
                tab-style="font-size: 18px;letter-spacing: 2px;margin: 0 10px 0 10px;font-weight: bold;">
                <n-tab-pane name="publish" tab="发布">
                    <n-result title="暂无内容" description="去别处看看吧" size="huge" v-if="getPublishEnd && PublishData.Total === 0">
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
                        <a v-for="video in PublishData.VideoList" :href="'#/?Vid=' + video.id" target="_blank">
                            <TinyCard :coverurl="video.coverUrl" :title="video.title" />
                        </a>
                        <div id="user-GetNext" v-show="!getPublishEnd">
                            <div v-if="getPublishing" class="user-Canva">
                                <n-spin size="large" />
                                <p>加载中</p>
                            </div>
                            <div v-else class="user-Canva">
                                <n-button circle @click="getPublished" size="large">
                                    <template #icon>
                                        <n-icon><svg xmlns="http://www.w3.org/2000/svg"
                                                xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 20 20">
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
                <n-tab-pane name="star" tab="收藏" v-if="IsSelf">
                    <n-result title="暂无内容" description="去别处看看吧" size="huge" v-if="getCollectEnd && CollectData.Total === 0">
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
                        <a v-for="video in CollectData.VideoList" :href="'#/?Vid=' + video.id" target="_blank">
                            <TinyCard :coverurl="video.coverUrl" :title="video.title" />
                        </a>
                        <div id="user-GetNext" v-show="!getCollectEnd">
                            <div v-if="getCollecting" class="user-Canva">
                                <n-spin size="large" />
                                <p>加载中</p>
                            </div>
                            <div v-else class="user-Canva">
                                <n-button circle @click="getCollect" size="large">
                                    <template #icon>
                                        <n-icon><svg xmlns="http://www.w3.org/2000/svg"
                                                xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 20 20">
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
        </div>
    </div>
</template>

<style>
.message-self {
    text-align: right;
}

.message-p {
    margin: 0;
    font-size: 15px;
}

#user-GetNext {
    height: 348px;
    width: 201px;
    display: flex;
    justify-content: center;
    align-items: center;
}

.user-Canva {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.edit-button-sub {
    height: 40px;
    width: 170px;
}

.edit-btn {
    margin-top: 5px;
    display: flex;
    justify-content: right;
}

#edit-avatar .n-thing-main .n-thing-main__description {
    display: flex;
    justify-content: center;
}

#edit-avatar .n-thing-main .n-thing-main__footer {
    display: flex;
    justify-content: center;
}

#edit-info .n-card-header .n-card-header__main {
    font-size: 30px;
    font-weight: bold !important;
}

.edit-info-title {
    font-size: 20px;
    font-weight: bold;
    display: block;
    width: 120px;
    top: -2px;
    position: relative;
}

.user-flow-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
    grid-gap: 20px;
    margin: 0 10px 0 10px;
}

#user-flow {
    margin-top: 13px;
}

#user-sig {
    font-size: 15px;
    margin: 5px 0 0 0;
}

.n-thing-header {
    display: flex;
    align-items: flex-start !important;
}

#user-name {
    font-size: 40px;
    font-weight: bold;
    margin: 30px 0 10px 0;
}

.edit-container {
    display: flex;
    justify-content: flex-start;
}

.edit-p {
    margin-left: 8px;
    font-size: 17px;
    font-weight: bold;
}

.edit-button {
    height: 40px;
    width: 170px;
    margin-right: 20px;
}
</style>