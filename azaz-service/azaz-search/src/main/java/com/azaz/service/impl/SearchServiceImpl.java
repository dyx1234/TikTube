package com.azaz.service.impl;


import com.alibaba.fastjson2.JSON;
import com.azaz.clients.VideoClient;
import com.azaz.exception.NullParamException;
import com.azaz.interact.vo.UserListVo;
import com.azaz.interact.vo.VideoListVo;
import com.azaz.response.ResponseResult;
import com.azaz.user.pojo.User;
import com.azaz.user.vo.UserPersonalInfoVo;
import com.azaz.utils.ThreadLocalUtil;
import com.azaz.video.pojo.Video;
import com.azaz.video.vo.VideoDetail;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 搜索模块实现类
 * @author shigc
 */
@Service
@Log4j2
public class SearchServiceImpl implements com.azaz.service.SearchService{

    @Resource
    private RestHighLevelClient restHighLevelClient;

    @Resource
    private VideoClient videoClient;

    /**
     *  搜索用户
     * @param keyword 关键字
     * @param page 页码,从1开始
     * @param pageSize 页大小
     * @return 用户列表
     */
    @Override
    public ResponseResult<UserListVo> searchUser(String keyword, Integer page, Integer pageSize) {
        if (keyword == null || keyword.length() < 2) {
            throw new NullParamException("搜索关键词长度需大于2个！");
        }
        log.info("搜索用户，关键字：{}，页码：{}，页大小：{}", keyword, page, pageSize);
        // 根据keyword搜索tb_user表，返回用户列表
        // 设置查询条件
        SearchRequest searchRequest = new SearchRequest("tb_user");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 设置查询关键词
        QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder(keyword);
        boolQueryBuilder.must(queryStringQueryBuilder);
        // 设置分页
        // 页码从1开始
        searchSourceBuilder.from((page - 1) * pageSize);
        // 页大小
        searchSourceBuilder.size(pageSize);
        // 设置排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置查询源
        searchRequest.source(searchSourceBuilder);
        // 执行查询
        List<UserPersonalInfoVo> userInfoList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 返回结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                User user = JSON.parseObject(json, User.class);
                log.info("搜索结果：{}", user);
                UserPersonalInfoVo userPersonalInfoVo = new UserPersonalInfoVo();
                BeanUtils.copyProperties(user, userPersonalInfoVo);
                userPersonalInfoVo.setId(user.getId().toString());
                userInfoList.add(userPersonalInfoVo);
            }
        } catch (Exception e) {
            log.error("搜索用户失败", e);
        }
        UserListVo userListVo = UserListVo.builder()
                .total(userInfoList.size())
                .list(userInfoList)
                .build();
        return ResponseResult.successResult(userListVo);
    }

    /**
     * 搜索视频
     * @param keyword 关键字
     * @param page 页码
     * @param pageSize 页大小
     * @return 视频列表
     */
    @Override
    public ResponseResult<VideoListVo> searchVideo(String keyword, Integer page, Integer pageSize) {
        if (keyword == null || keyword.length() < 2) {
            throw new NullParamException("搜索关键词长度需大于2个！");
        }
        log.info("搜索视频，关键字：{}，页码：{}，页大小：{}", keyword, page, pageSize);
        log.info("当前用户：{}", ThreadLocalUtil.getUserId());
        // 根据keyword搜索tb_user表，返回用户列表
        // 设置查询条件
        SearchRequest searchRequest = new SearchRequest("tb_video");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 设置布尔查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // 设置查询关键词
        QueryStringQueryBuilder queryStringQueryBuilder = new QueryStringQueryBuilder(keyword);
        boolQueryBuilder.must(queryStringQueryBuilder);
        // 设置分页
        // 页码从1开始
        searchSourceBuilder.from((page - 1) * pageSize);
        // 页大小
        searchSourceBuilder.size(pageSize);
        // 设置排序
        searchSourceBuilder.sort("id", SortOrder.DESC);
        // 设置查询条件
        searchSourceBuilder.query(boolQueryBuilder);
        // 设置查询源
        searchRequest.source(searchSourceBuilder);
        // 执行查询
        List<VideoDetail> videoDetailList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            // 返回结果
            SearchHit[] hits = searchResponse.getHits().getHits();
            for (SearchHit hit : hits) {
                String json = hit.getSourceAsString();
                Video video = JSON.parseObject(json, Video.class);
                log.info("搜索结果：{}", video);
                VideoDetail videoDetail = videoClient.getVideoDetailInfo(video.getId()).getData();
                videoDetailList.add(videoDetail);
            }
        } catch (Exception e) {
            log.error("搜索用户失败", e);
        }
        return ResponseResult.successResult(VideoListVo.builder()
                .total(videoDetailList.size())
                .videoList(videoDetailList)
                .build());
    }
}
