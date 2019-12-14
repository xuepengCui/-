package com.qingcheng.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonResponse;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @创建人 cxp
 * @创建时间 2019-11-8
 * @描述
 */
@Component
public class SmsMessageConsumer implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;


    @Value("${smsCode}")
    private String smsCode;   //短信模板编号

    @Value("${param}")        //短信参数
    private String param;

    public void onMessage(Message message) {

        String jsonString= new String(message.getBody());//得到mq中存入的json格式的消息

        Map<String,String> map = JSON.parseObject(jsonString, Map.class);//将json格式转换为Map格式

        String phone = map.get("phone");//mq中存入的手机号
        String code = map.get("code");//mq中存入的验证码
        System.out.println("手机号"+phone+"验证码"+code);


        //调用阿里云通信

        CommonResponse commonResponse = smsUtil.sendSms(phone, smsCode, param.replace("[value]", code));


    }
}
