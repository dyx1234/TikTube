<script setup>
const props = defineProps(['title', 'coverurl']);
</script>

<template>
    <div class="flow-card">
        <div class="flow-card-div">
            <img class="flow-card-img" :src="props.coverurl">
        </div>
        <div class="flow-card-info">
            <p class="flow-card-title">
                {{ props.title }}
            </p>
        </div>
    </div>
</template>

<style scoped>
.flow-card-info {
    position: relative;
    height: 80px;
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
}</style>