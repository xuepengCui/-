package com.qingcheng.service.impl;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * @创建人 cxp
 * @创建时间 2019-11-6
 * @描述
 */
public class RestClientFactory {

    public static RestHighLevelClient getRestHighLevelClient(String hostname, int port){

        HttpHost http=new HttpHost(hostname,port,"http");
        RestClientBuilder builder= RestClient.builder(http);//rest构建器
        return new RestHighLevelClient(builder);//高级客户端对象 （连接）

    }



}
