package com.csh.web.controller.system;

import com.alibaba.fastjson.JSON;
import com.csh.system.domain.TMember;
import com.csh.system.service.ITMemberService;
import com.csh.web.controller.wai.controller.utils.BaseResponse1;
import com.csh.web.controller.wai.controller.utils.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * 会员备份接口
 */
@Controller
@RequestMapping("/system/copy")
public class CopyController {

    @Autowired
    ITMemberService itMemberService;

    /**
     * 会员信息数据备份接口
     *
     */
    @ResponseBody
    @RequestMapping(value = "/backupData", method = {RequestMethod.POST},consumes = "application/json")
        public BaseResponse1 TMemberBakeUp(@RequestBody  String jsonData){
        TMember aa = JSON.parseObject(jsonData,TMember.class);
        long startTime = System.currentTimeMillis(); //获取开始时间
        //通过会员卡号查询会员信息，返回会员实体

       TMember tMember = itMemberService.selectTMemberByMemberNo(aa.getMemberNo());
        //问题出在这里
        //判断返回的实体，如有没有就插入，有就更新
        if(tMember!=null){
            int i = itMemberService.updateTMemberByMemberNo(aa);
            if(i>0){
                System.out.println("更新成功");
                BaseResponse1 baseResponse = new BaseResponse1(StatusCode.Gg);
                long endTime = System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间
                return  baseResponse;
            }else {
                BaseResponse1 baseResponse = new BaseResponse1(StatusCode.Mn);
                System.out.println("更新失败");
                long endTime = System.currentTimeMillis(); //获取结束时间
                return  baseResponse;
            }


        }else {

            //插入数据

            int y =itMemberService.insertTMember(aa);
            if(y>0){
                System.out.println("插入成功");
                BaseResponse1 baseResponse = new BaseResponse1(StatusCode.Hh);
                long endTime = System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间：" + (endTime - startTime) + "ms"); //输出程序运行时间

                return  baseResponse;

            }else {
                BaseResponse1 baseResponse = new BaseResponse1(StatusCode.Jj);
                System.out.println("插入失败");
                long endTime = System.currentTimeMillis(); //获取结束时间
                return  baseResponse;
            }


        }
       //JSONObject jsonObject = JSONObject.fromObject(tMember);




       }










    }




