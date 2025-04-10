<script setup>
import { ref, onMounted } from 'vue';
import { NResult, NIcon, NSelect, NText, NP, NUploadDragger, NUpload, NCard, NInput, NButton, NFormItem, NForm, NMessageProvider, NNotificationProvider } from 'naive-ui';
import axios from 'axios';
import notice from '../components/notice.vue';
import qs from 'qs';
import { is } from 'date-fns/locale';

const userAccount = ref({
    UserToken: null,
    UserId: null,
});

onMounted(() =>
{
    const account = localStorage.getItem('Useraccount');
    if (account)
    {
        userAccount.value = JSON.parse(localStorage.getItem('Useraccount'));
        axios.defaults.headers.common['token'] = userAccount.value.UserToken;//登陆后更新token
    }
});

async function beforeCoverUpload(data)
{
    if (data.file?.file.size > 1024 * 1024 * 50)
    {
        window.$message.error(
            '文件大小不应超过50M'
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

async function beforeVideoUpload(data)
{
    if (data.file?.file.size > 1024 * 1024 * 255)
    {
        window.$message.error(
            '文件大小不应超过255M'
        );
        return false;
    }
    else if (!data.file?.type.startsWith("video/"))
    {
        window.$message.error(
            '必须上传支持的视频文件类型。'
        );
        return false;
    }
    return true;
}

const uploadCover = ref(null);
const uploadVideo = ref(null);
const isValidate = ref(false);
const isUping = ref(false);
const UploadDone = ref(false);
const VideoTitle = ref(null);
const VideoSection = ref(null);
var fileNumber = 0;
var VideoUrl = "";
var CoverUrl = "";
var isVideoUploading = false;
var isCoverUploading = false;

function fileChange(options)
{
    fileNumber = options.fileList.length;
    if (fileNumber !== 1 || !VideoSection.value || !VideoTitle.value)
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
    if (fileNumber !== 1 || !VideoSection.value || !VideoTitle.value)
    {
        isValidate.value = false;
    }
    else
    {
        isValidate.value = true;
    }
}

async function publish()
{
    try 
    {
        // 发送请求
        const editurl = '/azaz/video/publish?' + qs.stringify({
            title: VideoTitle.value,
            videoUrl: VideoUrl,
            coverUrl: CoverUrl,
            section: VideoSection.value,
        });
        const response = await axios.post(editurl);

        // 处理响应
        const responseData = response.data;
        if (responseData.code === 2)
        {
            // 成功
            window.$message.success(
                '视频发布成功'
            );
            isUping.value = false;
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
            '发布失败，网络异常，请稍后重试。'
        );
    }
}

function uploadProcess()
{
    isUping.value = true;
    uploadVideo.value?.submit();
    uploadCover.value?.submit();
    const intervalId = setInterval(() =>
    {
        if (!isVideoUploading && !isCoverUploading)
        {
            publish();
            isUping.value = false;
            UploadDone.value = true;
            setTimeout(() => { location.reload(true); }, 3000);
            clearInterval(intervalId);
        }
    }, 1000);
}

const videoUpload = async ({ file, onFinish, onError, onProgress }) =>
{
    const formData = new FormData();
    formData.append('file', file.file);

    try
    {
        isVideoUploading = true;
        const response = await axios.post('/azaz/video/upload', formData, {
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
            VideoUrl = responseData.data;
            onFinish();
        } else
        {
            window.$message.error(
                responseData.message
            );
            onError();
        }
        isVideoUploading = false;
    } catch (error)
    {
        // 网络请求失败
        window.$message.error(
            '网络异常，请稍后重试。'
        );
        onError();
        isVideoUploading = false;
    }
};

const coverUpload = async ({ file, onFinish, onError, onProgress }) =>
{
    const formData = new FormData();
    formData.append('file', file.file);

    try
    {
        isCoverUploading = true;
        const response = await axios.post('/azaz/video/upload', formData, {
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
</script>

<template>
    <n-message-provider><n-notification-provider>
            <notice />
        </n-notification-provider></n-message-provider>
    <n-result style="margin-top: 240px;" status="success" title="发布成功" description="请等待页面自动刷新或手动刷新" v-show="UploadDone">
    </n-result>
    <div style="display: flex;justify-content: center;" v-show="!UploadDone">
        <n-card title="上传视频" embedded bordered="false" hoverable id="upload-card" style="max-width: 850px;">
            <div id="upload-container">
                <n-form label-placement="left" label-width="auto" require-mark-placement="right-hanging" size="large"
                    style="maxWidth: 640px;">
                    <n-form-item label="视频文件" path="video">
                        <n-upload :show-cancel-button="!isUping" :disabled="isUping" :show-download-button="false"
                            @before-upload="beforeVideoUpload" :show-remove-button="false" :custom-request="videoUpload"
                            @change="fileChange" :default-upload="false" max="1" ref="uploadVideo">
                            <n-upload-dragger
                                style="background-color: white;height: 300px;display: flex;justify-content: center; flex-direction: column;">
                                <n-text style="font-size: 16px">
                                    点击或者拖动视频文件到该区域来上传
                                </n-text>
                                <n-p depth="3" style="margin: 8px 0 0 0">
                                    只能上传一条视频
                                </n-p>
                            </n-upload-dragger>
                        </n-upload>
                    </n-form-item>
                    <n-form-item label="视频封面" path="cover">
                        <n-upload :show-cancel-button="!isUping" :disabled="isUping" :show-download-button="false"
                            @before-upload="beforeCoverUpload" :show-remove-button="false" :custom-request="coverUpload"
                            :default-upload="false" max="1" ref="uploadCover">
                            <n-upload-dragger
                                style="background-color: white;height: 150px;display: flex;justify-content: center; flex-direction: column;">
                                <n-text style="font-size: 16px">
                                    点击或者拖动视频封面（图片文件）到该区域来上传
                                </n-text>
                                <n-p depth="3" style="margin: 8px 0 0 0">
                                    视频封面不是必须的，如果不上传封面，系统将自动为您生成
                                </n-p>
                            </n-upload-dragger>
                        </n-upload>
                    </n-form-item>
                    <n-form-item label="视频标题" path="title">
                        <n-input :disabled="isUping" @change="checkVideo" v-model:value="VideoTitle" show-count
                            placeholder="请输入视频标题，最多50字" :maxlength="50" :count-graphemes="countGraphemes" />
                    </n-form-item>
                    <n-form-item label="视频分类" path="section">
                        <n-select :disabled="isUping" v-model:value="VideoSection" :options="options" placeholder="请选择视频分类"
                            @update:value="checkVideo" />
                    </n-form-item>


                    <div style="display: flex; justify-content: flex-end">
                        <n-button @click="uploadProcess" style="height: 40px;width: 170px;"
                            :disabled="!isValidate || isUping">
                            <n-icon size="20">
                                <svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
                                    viewBox="0 0 512 512">
                                    <path
                                        d="M374.79 308.78L457.5 367a16 16 0 0 0 22.5-14.62V159.62A16 16 0 0 0 457.5 145l-82.71 58.22A16 16 0 0 0 368 216.3v79.4a16 16 0 0 0 6.79 13.08z"
                                        fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"
                                        stroke-width="32"></path>
                                    <path
                                        d="M268 384H84a52.15 52.15 0 0 1-52-52V180a52.15 52.15 0 0 1 52-52h184.48A51.68 51.68 0 0 1 320 179.52V332a52.15 52.15 0 0 1-52 52z"
                                        fill="none" stroke="currentColor" stroke-miterlimit="10" stroke-width="32"></path>
                                </svg>
                            </n-icon>
                            <p style="margin-left: 8px;font-size: 17px;font-weight: bold;">开始上传</p>
                        </n-button>
                    </div>
                </n-form>
            </div>
        </n-card>
    </div>
</template>
<style>
#upload-card .n-card-header .n-card-header__main {
    font-size: 30px !important;
    font-weight: bold !important;
}
</style>

<script>
const options = [
    {
        label: "直播",
        value: "1"
    },
    {
        label: "体育",
        value: "2"
    }, {
        label: "游戏",
        value: "3"
    }, {
        label: "番剧",
        value: "4"
    }, {
        label: "知识",
        value: "5"
    }, {
        label: "娱乐",
        value: "6"
    }, {
        label: "美食",
        value: "7"
    }, {
        label: "时尚",
        value: "8"
    }, {
        label: "热点",
        value: "9"
    },
];
</script>
