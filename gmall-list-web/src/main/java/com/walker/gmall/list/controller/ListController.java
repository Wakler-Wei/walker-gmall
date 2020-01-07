package com.walker.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.bean.*;
import com.walker.gmall.service.ListService;
import com.walker.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Walker
 * @Date 2020/1/5 18:58
 * @Version 1.0
 */
@Controller
public class ListController {

    // http://list.gmall.com/list.html?catalog3Id=61

    @Reference
    private ListService listService;

    @Reference
    ManageService manageService;

    @RequestMapping("list.html")
    //@ResponseBody
    public String list(SkuLsParams skuLsParams, Model model){
        //设置分页
        skuLsParams.setPageSize(3);
        //全局搜索Elasticsearch方法调用
        SkuLsResult skuLsResult = listService.search(skuLsParams);

        //商品集合
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        model.addAttribute("skuLsInfoList",skuLsInfoList);

        //查询平台属性
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList = manageService.attrInfoList(attrValueIdList);
        model.addAttribute("baseAttrInfoList",baseAttrInfoList);

        //平台属性值过滤
        String urlParam = makeUrlParam(skuLsParams);
        //将添加之后的平台属性移除并添加到面包屑中
        // 声明一个保存面包屑的集合
        List<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();
        //平台属性集合
        if (baseAttrInfoList !=null && baseAttrInfoList.size()>0){
            //itco迭代器
            for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                //平台属性值集合
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                //获取每一个平台属性值
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    //获取地址栏中参数的平台属性值
                    if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length>0){
                        //若存在平台属性值，获取该值
                        for (String valueId : skuLsParams.getValueId()) {
                            //选中的属性值 和 查询结果的属性值作比较
                            //若是为TRUE则移除平台属性及平台属性值
                            if (baseAttrValue.getId().equals(valueId)){
                                iterator.remove();

                            //新建一个集合用来存面包屑
                            BaseAttrValue baseAttrValueed = new BaseAttrValue();
                            // 将面包屑的内容 赋值给了平台属性值对象的名称
                            baseAttrValueed.setValueName(baseAttrInfo.getAttrName() + ":" + baseAttrValue.getValueName());
                            //新的UrlParam
                            String newUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueed.setUrlParam(newUrlParam);
                            // 将每个面包屑都放入集合中！
                            baseAttrValueArrayList.add(baseAttrValueed);

                            }
                        }
                    }
                }
            }
        }


        //参数
        model.addAttribute("urlParam",urlParam);

        //面包屑数据
        model.addAttribute("keyword",skuLsParams.getKeyword());
        model.addAttribute("baseAttrValueArrayList",baseAttrValueArrayList);

        //分页数据
        model.addAttribute("totalPages",skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());
        //return JSON.toJSONString(skuLsResult);
        return "list";
    }

    /**
     * 制作地址栏中参数
     * @param skuLsParams
     * @return
     * 重新发起请求
     *     未点击之前： href=http://list.gmall.com/list.html?catalog3Id=61
     *     点击一下之后： http://list.gmall.com/list.html?catalog3Id=61&valueId=82
     *     再点击一次：http://list.gmall.com/list.html?catalog3Id=61&valueId=82&valueId=81
     *     urlParam=catalog3Id=61
     *     href="#" href="http://list.gmall.com/list.html?${urlParam}+"&valueId="attrValue.id"
     *
     */
    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {

        String urlParam="";
        // 判断用户是否输入的三级分类Id
        // http://list.gmall.com/list.html?catalog3Id=61
        if (skuLsParams.getCatalog3Id()!=null && skuLsParams.getCatalog3Id().length()>0){
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }
        // 判断用户是否输入的是keyword！
        // http://list.gmall.com/list.html?keyword=手机
        if (skuLsParams.getKeyword() !=null && skuLsParams.getKeyword().length()>0){
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }
        // 判断用户是否输入的平台属性值Id 检索条件
        // http://list.gmall.com/list.html?catalog3Id=61&valueId=82
        if (skuLsParams.getValueId() !=null &&skuLsParams.getValueId().length>0){
            for (String valueId : skuLsParams.getValueId()) {

                if (excludeValueIds !=null &&excludeValueIds.length>0){
                    // 获取对象中的第一个数据
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // 停止当前循环
                        continue;
                    }
                }
//                if (urlParam.length()>0){
//                    urlParam+="&";
//                }
                    urlParam +="&valueId=" +valueId;
            }
        }


        return urlParam;
    }


}
