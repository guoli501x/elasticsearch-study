package com.guoli.search.service;

import java.util.List;
import java.util.Map;

/**
 * 功能描述
 *
 * @author guoli
 * @data 2021-03-06 23:00
 */
public interface ContentService {
    /**
     * 爬取数据到Elasticsearch
     *
     * @param keyword 关键字
     * @return 返回提示信息
     */
    String parseDataToEs(String keyword);

    /**
     * 从es搜索
     *
     * @param keyword 搜索关键字
     * @param from 分页起始位置
     * @param size 分页大小
     * @return 查询到的数据
     */
    List<Map<String, Object>> searchPage(String keyword, int from, int size);
}
