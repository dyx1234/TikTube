package com.azaz.mq;

import com.alibaba.fastjson2.JSON;
import com.azaz.user.pojo.User;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 用户信息监听器
 * 用于更新用户信息到es中
 * @author shigc
 */
@Log4j2
@Component
@RocketMQMessageListener(topic = "user_info", consumerGroup = "user_group")
public class UserInfoListener implements RocketMQListener<User> {

    @Resource
    private RestHighLevelClient restHighLevelClient;


    @Override
    public void onMessage(User user) {
        if (StringUtils.isBlank(user.getUsername())) {
            return;
        }
        log.info("ES接收到用户信息变化消息:{}", user);
        try {
            // 指定索引名
            IndexRequest indexRequest = new IndexRequest("tb_user");
            // 指定文档id
            indexRequest.id(user.getId().toString());
            // 指定文档内容
            indexRequest.source(JSON.toJSONString(user), XContentType.JSON);
            // 执行索引请求
            IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
            if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                log.info("用户信息文档创建成功，索引名：{}，文档id：{}", indexResponse.getIndex(), indexResponse.getId());
            } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                log.info("用户信息文档更新成功，索引名：{}，文档id：{}", indexResponse.getIndex(), indexResponse.getId());
            }
        } catch (IOException e) {
            log.error("索引文档失败，索引名：{}，文档id：{}", "tb_video", user.getId(), e);
        }
    }
}
