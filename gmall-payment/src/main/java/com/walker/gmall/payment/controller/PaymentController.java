package com.walker.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.walker.gmall.LoginRequire;
import com.walker.gmall.bean.OrderInfo;
import com.walker.gmall.bean.PaymentInfo;
import com.walker.gmall.bean.enums.PaymentStatus;
import com.walker.gmall.payment.config.AlipayConfig;
import com.walker.gmall.service.OrderService;
import com.walker.gmall.service.PaymentService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.apache.catalina.manager.Constants.CHARSET;


/**
 * @Author Walker
 * @Date 2020/1/12 20:56
 * @Version 1.0
 */
@Controller
public class PaymentController {

    @Reference
    OrderService orderService;

    @Autowired
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    @LoginRequire
    public  String index(HttpServletRequest request){
        // 获取订单的id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("orderId",orderId);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());
        return "index";

    }

    // http://payment.gmall.com/alipay/submit
    @RequestMapping("alipay/submit")
    @ResponseBody // 将数据直接渲染到页面！
    public String aliPaySubmit(HttpServletRequest request, HttpServletResponse response){
        /*
        1.  保存交易记录
        2.  生成二维码
         */
        // 获取订单Id
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setSubject("哈哈哈！");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentService.savyPaymentInfo(paymentInfo);


        //  生成二维码
        // AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE); //获得初始化的AlipayClient
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        // 设置同步回调url
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 设置异步回调url
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);//在公共参数中设置回跳和通知地址
        // 将参数封装到alipayRequest 做成二维码 Json 字符串
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        alipayRequest.setBizContent(JSON.toJSONString(map));
        String form="";
        try {
            form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=" + CHARSET);
        return form;

    }

    // 支付成功之后重定向到订单url
    @RequestMapping("alipay/callback/return")
    public String callBack(){
        // 回调到订单页面
        return "redirect:"+AlipayConfig.return_order_url;
    }


    // 异步回调
    // http://xxx.xxx.xxx/index?total_amout=0.01
    @RequestMapping("alipay/callback/notify")
    @ResponseBody
    public String notifyUrl(@RequestParam Map<String,String> paramMap, HttpServletRequest request) throws AlipayApiException {
        System.out.println("你回来啦！");
        // 只有交易通知状态为 TRADE_SUCCESS 或 TRADE_FINISHED 时，支付宝才会认定为买家付款成功
        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");

        boolean signVerified = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, CHARSET, AlipayConfig.sign_type); //调用SDK验证签名
        if(signVerified){
            // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure

            if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)){
                // 如果paymentInfo 中payment_status 是PAID 或者CLOSE 那么这个时候，也应该是失败！
                // 根据out_trade_no 查询 paymentInfo 对象
                // select * from paymentInfo where out_trade_no = ?
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setOutTradeNo(out_trade_no);
                PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
                if (paymentInfoQuery.getPaymentStatus()==PaymentStatus.PAID || paymentInfoQuery.getPaymentStatus()==PaymentStatus.ClOSED){
                    return "failure";
                }

                // 更新交易记录状态，改为付款！paymentInfo
                // update paymentInfo set payment_status = PAID ,callback_time = ? where out_trade_no = ?
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                paymentInfoUpd.setCallbackTime(new Date());
                paymentService.updatePaymentInfo(out_trade_no,paymentInfoUpd);
                return "success";
            }
        }else{
            // TODO 验签失败则记录异常日志，并在response中返回failure.
            return "failure";
        }
        return "failure";
    }


    // 根据订单Id 退款
    // http://payment.gmall.com/refund?orderId=118
    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId){
        // 退款业务
        boolean flag =  paymentService.refund(orderId);

        return ""+flag;
    }


}
