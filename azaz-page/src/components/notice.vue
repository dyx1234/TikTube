<template></template>

<script>
import { useMessage, useNotification } from 'naive-ui';
import { defineComponent } from 'vue';

export default defineComponent({
    setup()
    {
        window.$message = useMessage();
        window.$notification = useNotification();
    }
});
</script>