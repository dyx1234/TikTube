<script setup>
import { RouterLink } from 'vue-router';
const props = defineProps(['commentsnumber', 'title', 'upname', 'likenumber', 'imgurl', 'coverurl', 'index', 'userid']);
const emit = defineEmits(['showplay']);

function play()
{
    emit('showplay', props.index);
}
</script>

<template>
    <div class="flow-card" @click="play">
        <div class="flow-card-div">
            <img class="flow-card-img" :src="props.coverurl">
            <div class="flow-card-data">
                <svg class="flow-card-play" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 28 28"><g fill="none"><path d="M6 6.01v-.25a3.75 3.75 0 0 1 3.75-3.75h10.5A5.75 5.75 0 0 1 26 7.76v6.99a3.75 3.75 0 0 1-3.75 3.75H22v.25a3.75 3.75 0 0 1-3.75 3.75h-5.997l-4.245 3.235c-.823.627-2.008.04-2.008-.994V22.5h-.25A3.75 3.75 0 0 1 2 18.75V9.76a3.75 3.75 0 0 1 3.75-3.75H6zm1.5-.25v.25h10.75A3.75 3.75 0 0 1 22 9.76V17h.25a2.25 2.25 0 0 0 2.25-2.25V7.76a4.25 4.25 0 0 0-4.25-4.25H9.75A2.25 2.25 0 0 0 7.5 5.76zm-4 4v8.99A2.25 2.25 0 0 0 5.75 21h1a.75.75 0 0 1 .75.75v2.486l4.046-3.082A.75.75 0 0 1 12 21h6.25a2.25 2.25 0 0 0 2.25-2.25V9.76a2.25 2.25 0 0 0-2.25-2.25H5.75A2.25 2.25 0 0 0 3.5 9.76z" fill="currentColor"></path></g></svg>
                <span class="flow-card-playnumber">{{ props.commentsnumber }}</span>
            </div>
        </div>
        <div class="flow-card-info">
            <p class="flow-card-title">
                {{ props.title  }}
            </p>
            <div class="flow-card-up">
                <RouterLink :to="'/user/' + props.userid" class="flow-card-upinfo">
                    <img class="flow-card-upimg" :src="props.imgurl">
                    <span class="flow-card-upname">{{ props.upname }}</span>
                </RouterLink>
                <div class="flow-card-like">
                    <svg class="flow-card-likeicon" xmlns="http://www.w3.org/2000/svg"
                        xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 0 24 24">
                        <g fill="none">
                            <path
                                d="M16.5 5.203c0-2.442-1.14-4.2-3.007-4.2c-1.026 0-1.378.602-1.746 2c-.075.29-.112.43-.151.569c-.101.358-.277.97-.527 1.83a.25.25 0 0 1-.03.065L8.174 9.953a5.885 5.885 0 0 1-2.855 2.327l-.473.181a2.75 2.75 0 0 0-1.716 3.092l.404 2.086a3.25 3.25 0 0 0 2.417 2.538l7.628 1.87a4.75 4.75 0 0 0 5.733-3.44l1.415-5.55a3.25 3.25 0 0 0-3.15-4.053h-1.822c.496-1.633.746-2.892.746-3.801zM4.6 15.267a1.25 1.25 0 0 1 .78-1.405l.474-.181a7.384 7.384 0 0 0 3.582-2.92l2.867-4.486c.09-.14.159-.294.205-.454c.252-.865.428-1.48.53-1.843c.044-.153.085-.309.159-.593c.19-.722.283-.881.295-.881c.868 0 1.507.984 1.507 2.699c0 .884-.326 2.335-.984 4.315a.75.75 0 0 0 .711.986h2.85a1.75 1.75 0 0 1 1.696 2.182l-1.415 5.55a3.25 3.25 0 0 1-3.923 2.353l-7.628-1.87a1.75 1.75 0 0 1-1.301-1.366L4.6 15.267z"
                                fill="currentColor"></path>
                        </g>
                    </svg>
                    <span class="flow-card-likenumber">{{ props.likenumber }}</span>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.flow-card-likenumber {
    font-size: 15px;
}

.flow-card-likeicon {
    width: 20px;
    height: 20px;
    margin-right: 4px;
    margin-bottom: 2px;
}

.flow-card-upname {
    margin-left: 8px;
    font-size: 15px;
    max-width: 95px;
    overflow:hidden;

text-overflow:ellipsis;

white-space:nowrap;
}

.flow-card-upimg {
    width: 23px;
    height: 23px;
    border-radius: 50%;
}

.flow-card-upinfo {
    display: flex;
    align-items: flex-end;
    justify-content: flex-start;
}

.flow-card-like {
    display: flex;
    align-items: flex-end;
    justify-content: flex-end;
}

.flow-card-info {
    position: relative;
    height: 80px;
}

.flow-card-up {
    position: absolute;
    bottom: 0;
    left: 0;
    width: 201px;
    height: 30px;
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
}

.flow-card-title {
    margin-top: 10px;
    font-size: 15px;
    width: 201px;
    word-break: break-all;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
}

.flow-card-playnumber {
    color: #ffffff;
    margin-left: 8px;
    margin-bottom: 8px;
    font-size: 16px;
}

.flow-card-play {
    color: #ffffff;
    width: 25px;
    height: 25px;
    margin-left: 8px;
    margin-bottom: 8px;
}

.flow-card-data {
    width: 100%;
    height: 40%;
    position: absolute;
    bottom: 0;
    left: 0;
    display: flex;
    align-items: flex-end;
    justify-content: flex-start;
    background-image: linear-gradient(to top, rgba(0, 0, 0, 0.2), transparent);
}

.flow-card-div {
    position: relative;
    width: 201px;
    height: 268px;
    border-radius: 10px;
    overflow: hidden;
    background-color: #efefef;
}

.flow-card-img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}
</style>