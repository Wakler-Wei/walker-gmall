package com.walker.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.walker.gmall.bean.*;
import com.walker.gmall.config.RedisUtil;
import com.walker.gmall.manage.context.ManageConst;
import com.walker.gmall.manage.mapper.*;
import com.walker.gmall.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    // 调用mapper
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Resource  //默认查找type
    private  SpuInfoMapper spuInfoMapper;

    @Resource  // 默认按照name，如果没有name 找type
    private  BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Resource
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private  SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private  SkuImageMapper skuImageMapper;

    @Autowired
    private RedisUtil redisUtil;


    @Override
    public List<BaseCatalog1> getCatalog1() {
        // select * from basecatalog1 ;
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        //select * from baseCatalog2 where catalog1Id=?

        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(BaseCatalog3 baseCatalog3) {
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(String catalog3Id) {
        /*Example example = new Example(BaseAttrInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return baseAttrInfoMapper.selectByExample(example);*/
        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            // 修改：
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            // 直接保存平台属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }
        // baseAttrValue 修改：
        // 先将原有的数据删除，然后再新增！
        BaseAttrValue baseAttrValueDel = new BaseAttrValue();
        // delete from baseAttrValue where attrId = baseAttrInfo.getId();
        baseAttrValueDel.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDel);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        if (attrValueList != null && attrValueList.size() > 0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }

        }

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(BaseAttrValue baseAttrValue) {
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public List<BaseAttrValue> getBaseAttrInfo(BaseAttrValue baseAttrValue) {

        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(baseAttrValue.getAttrId());

        baseAttrInfo.setAttrValueList(getAttrValueList(baseAttrValue));

        return baseAttrInfo.getAttrValueList();
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        Example example = new Example(SpuInfo.class);
        example.createCriteria().andEqualTo("catalog3Id",catalog3Id);
        return spuInfoMapper.selectByExample(example);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
         /*
            spuInfo
            spuImage
            spuSaleAttr
            spuSaleAttrValue
         */
        spuInfoMapper.insertSelective(spuInfo);
        // spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList!=null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                // 赋值spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        // 销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList!=null && spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // 销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList!=null && spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

    }

    @Override
    public List<SpuImage> spuImageList(String spuId) {
        Example example = new Example(SpuImage.class);
        example.createCriteria().andEqualTo("spuId",spuId);
        List<SpuImage> spuImages = spuImageMapper.selectByExample(example);
        return spuImages;
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {

        skuInfoMapper.insertSelective(skuInfo);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size()>0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size()>0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }

        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size()>0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }

        }

    }

    @Override
    public SkuInfo getSkuInfo(String skuId){

        //SkuInfo skuInfoDB = getSkuInfoDB(skuId);   //数据库中获取
        //SkuInfo skuInfoNormore = getSkuInfoNormore(skuId);  //redis首次设置
        //SkuInfo skuInfoByRedisSet = getSkuInfoByRedisSet(skuId); //解决缓存击穿问题：
        SkuInfo skuInfoByRedisson = getSkuInfoByRedisson(skuId);

        return skuInfoByRedisson;

    }

    /**
     * 解决缓存击穿问题：Redisson框架解决
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoByRedisson(String skuId) {

        // 业务代码
        SkuInfo skuInfo =null;
        RLock lock =null;
        Jedis jedis =null;
        try {

            // 测试redis String
            jedis = redisUtil.getJedis();

            // 定义key
            String userKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if (jedis.exists(userKey)){
                // 获取缓存中的数据
                String userJson = jedis.get(userKey);
                if (!StringUtils.isEmpty(userJson)){
                    skuInfo = JSON.parseObject(userJson, SkuInfo.class);
                    return skuInfo;
                }
            }else {
                // 创建config
                Config config = new Config();
                // redis://192.168.67.220:6379 配置文件中！
                config.useSingleServer().setAddress("redis://192.168.111.131:6379");

                RedissonClient redisson = Redisson.create(config);

                lock = redisson.getLock("my-lock");

                lock.lock(10, TimeUnit.SECONDS);

                // 从数据库查询数据
                skuInfo = getSkuInfoDB(skuId);
                // 将数据放入缓存
                // jedis.set(userKey,JSON.toJSONString(skuInfo));
                jedis.setex(userKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
            if (lock!=null){
                lock.unlock();
            }

        }
        // 从db走！
        return getSkuInfoDB(skuId);


    }
    /**
     * 解决缓存击穿问题：lua脚本解决
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoByRedisSet(String skuId) {
        SkuInfo skuInfo = null;
        try{
            Jedis jedis = redisUtil.getJedis();
            // 定义key
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX; //key= sku:skuId:info

            String skuJson = jedis.get(skuInfoKey);

            if (skuJson==null || skuJson.length()==0){
                // 没有数据 ,需要加锁！取出完数据，还要放入缓存中，下次直接从缓存中取得即可！
                System.out.println("没有命中缓存");
                // 定义key user:userId:lock
                String skuLockKey=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 生成锁
                String lockKey  = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    System.out.println("获取锁！");
                    // 从数据库中取得数据
                    skuInfo = getSkuInfoDB(skuId);
                    // 将是数据放入缓存
                    // 将对象转换成字符串
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                    jedis.close();
                    return skuInfo;
                }else {
                    System.out.println("等待！");
                    // 等待
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }
            }else{
                // 有数据
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                jedis.close();
                return skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        // 从数据库返回数据
        return getSkuInfoDB(skuId);
    }


    /**
     * redis首次设置
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoNormore(String skuId){

        // 缓存测试-
        Jedis jedis = redisUtil.getJedis();
        // Ctrl+Alt+M 提取方法
        SkuInfo skuInfo=null;
        String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
        if (jedis.exists(skuInfoKey)){ //判断当前skuInfoKey是否存在
            // 取出数据
            String skuInfoJson = jedis.get(skuInfoKey);
            if (skuInfoJson!=null && skuInfoJson.length()!=0){
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
            }
            // 将数据转换成对象
        }else{
            // 从数据库中取得数据
            skuInfo = getSkuInfoDB(skuId);
            // 将最新的数据放入到缓存中
            String jsonString = JSON.toJSONString(skuInfo);
            jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,jsonString);
        }
        jedis.close();
        return skuInfo;
    }

    /**
     * 数据库中获取
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoDB(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);

        skuInfo.setSkuAttrValueList(skuAttrValues);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public List<BaseAttrInfo> attrInfoList(List<String> attrValueIdList) {

        // 将集合变成字符串！
        String attrValueIds = org.apache.commons.lang3.StringUtils.join(attrValueIdList.toArray(), ",");
        System.err.println("平台属性值Id集合字符串" + attrValueIds);
        return baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);

    }

}
