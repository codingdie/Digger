package com.codingdie.digger.util;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Created by xupeng on 2017/5/15.
 */
public class MailUtil {
    public  static  void  sendMail(String title,String content){
        try {
            Properties props = new Properties();
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

            // 开启debug调试
//            props.setProperty("mail.debug", "true");
            // 发送服务器需要身份验证
            props.setProperty("mail.smtp.auth", "true");
            // 设置邮件服务器主机名
            props.setProperty("mail.HOST", "smtp.qq.com");
            // 发送邮件协议名称
            props.setProperty("mail.transport.protocol", "smtp");
            props.setProperty("mail.smtp.PORT", "465");
            props.setProperty("mail.smtp.socketFactory.PORT", "465");
            props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
            // 设置环境信息
            Session session = Session.getInstance(props);
            // 创建邮件对象
            Message msg = new MimeMessage(session);
            msg.setSubject(title);
            // 设置邮件内容
            msg.setText(content);
            // 设置发件人
            msg.setFrom(new InternetAddress("547912355@qq.com"));

            Transport transport = session.getTransport();
            // 连接邮件服务器
            transport.connect("547912355@qq.com", "qygmbrakthsbbbff");
            // 发送邮件
            transport.sendMessage(msg, new Address[] {new InternetAddress("xupeng@pickme.cn")});
            // 关闭连接
            transport.close();
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }
}
