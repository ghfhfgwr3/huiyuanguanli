package com.csh.web.controller.system;

import java.math.BigDecimal;
import java.util.List;

import com.csh.common.utils.DateUtilsStr;
import com.csh.system.domain.*;
import com.csh.system.service.*;
import org.apache.ibatis.annotations.Param;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.csh.common.annotation.Log;
import com.csh.common.core.controller.BaseController;
import com.csh.common.core.domain.AjaxResult;
import com.csh.common.enums.BusinessType;
import com.csh.common.utils.DateUtils;
import com.csh.framework.util.ShiroUtils;

@Controller
@RequestMapping("/system/recharge")
public class RechargeController extends BaseController {
    private String prefix = "system/recharge";

    @Autowired
    private ITMemberService tMemberService;

    @Autowired
    private ITBalanceChangeService tBalanceChangeService;

    @Autowired
    private ITCardReplacementService tCardReplacementService;

    @Autowired
    private ITGiveruleService itGiveruleService;

    @Autowired
    private ISysUserService userService;

    @RequiresPermissions("system:recharge:view")
    @GetMapping()
    public String tBalanceChange() {
        return prefix + "/recharge";
    }

    /**
     * 修改会员余额的增加和减少
     */
    @RequiresPermissions("system:recharge:list")
    @PostMapping("/list")
    @ResponseBody
    public TMember edit(@Param("memberNo") String memberNo, @Param("mobilephones") String mobilephones, ModelMap mmap) {
        startPage();
        TMember list = new TMember();
        if (tMemberService.selectMemberNoAndPhone(memberNo, mobilephones) == null) {
            list.setMemberNo("不存在");

        } else {
            list = tMemberService.selectMemberNoAndPhone(memberNo, mobilephones);
        }
        return list;
    }

    @PostMapping("/offTMember")
    @ResponseBody
    public TMember offTMember(@Param("memberNo") String memberNo, @Param("mobilephones") String mobilephones) {
        TMember tMember = new TMember();
        if (tMemberService.selectMemberNoAndPhone(memberNo, mobilephones) == null) {
            tMember.setMemberNo("不存在");
        } else {
            tMember = tMemberService.selectMemberNoAndPhone(memberNo, mobilephones);
        }
        return tMember;
    }

    @PostMapping("/scbct")
    @ResponseBody
    public List<TGiverule> selectCompanyidByCardType(@Param("companyid") String companyid, @Param("cardtype") String cardtype) {
        //System.out.println(companyid+"**********"+cardtype);
        List<TGiverule> list_tGiverule = itGiveruleService.selectTGiveruleByCompanyidAndCardType(Integer.valueOf(companyid), Integer.valueOf(cardtype));

        return list_tGiverule;
    }

    @Log(title = "会员", businessType = BusinessType.UPDATE)
    @RequiresPermissions("system:recharge:money")
    @PostMapping("/update")
    @ResponseBody
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult editSave(@Param("memberNo") String memberNo, @Param("mobilephones") String mobilephones, @Param("balance") String balance, @Param("paytype") String paytype, @Param("m") String m, @Param("money") String money) throws Exception {
        TMember tMember;
        tMember = tMemberService.selectMemberNoAndPhone(memberNo, mobilephones);
        BigDecimal i = new BigDecimal(balance);//充值金额 或可退金额
        // System.out.println(i);


        BigDecimal MoneyOne = new BigDecimal(0);
        BigDecimal MoneyTwo = new BigDecimal(0);
        BigDecimal MoneyThree = new BigDecimal(0);

        BigDecimal returnMoney = new BigDecimal(0);
        BigDecimal changeTotalMoney = new BigDecimal(0);
        BigDecimal changeMoney = new BigDecimal(0);
        BigDecimal complimentaryMoney = new BigDecimal(0);
        BigDecimal tempMoney = new BigDecimal(0);//定义一个金额为零的字段
        BigDecimal onetimeMoney = new BigDecimal(0);//定义一个一次退款的金额


        BigDecimal points = new BigDecimal(0);
        TBalanceChange tBalanceChange = new TBalanceChange();
        Integer companyid = 0;
        String BalanceMemberNo = "";
        int updateTMemberNoAndPhone = 0;

        //
        points = tMember.getPoints();
        companyid = Integer.valueOf(tMember.getCompanyid().toString());
        BalanceMemberNo = tMember.getMemberNo();

        SysUser user = ShiroUtils.getSysUser();
        tBalanceChange.setWorkerid(user.getUserName());//获取用户
        tBalanceChange.setPayorderNO(DateUtils.dateTimeNow() + user.getUserId());
        tBalanceChange.setCompanyid(Long.valueOf(companyid));
        tBalanceChange.setMemberNo(BalanceMemberNo);

        if (m.equals("1")) {
            BigDecimal o = new BigDecimal(money);//总计金额
            BigDecimal c = o.subtract(i);//奖励金额
            tBalanceChange.setChangeMoney(i);//充值金额
            tBalanceChange.setChangeToltalMoney(o);
            tBalanceChange.setComplimentaryMoney(o.subtract(i));
            MoneyOne = tMember.getBalance().add(i);//充值金额

            MoneyTwo = tMember.getMoney().add(o);//总计

            MoneyThree = tMember.getComplimentaryMoney().add(c);//奖励

            tBalanceChange.setMemo("0");
            tBalanceChange.setPaytype(Integer.valueOf(paytype));
            try{
                int b = tBalanceChangeService.insertTBalanceChange(tBalanceChange);
                if (b==1){
                    try{
                        updateTMemberNoAndPhone = tMemberService.updateTMemberNoAndPhone(memberNo, mobilephones, MoneyOne.toString(), points.toString(), MoneyTwo.toString(), MoneyThree.toString());
                    }catch (Exception e){
                        return error("修改会员余额失败");
                    }
                }
            }catch (Exception e){
                return error("插入余额变化数据失败");
            }

        } else if (m.equals("0")) {
            int s = 0;
            while (true) {
                s = s+1;
                TBalanceChange query_tbalanceChange = tBalanceChangeService.selectBalanceObject(memberNo);
                tMember = tMemberService.selectMemberNoAndPhone(memberNo, mobilephones);
                changeTotalMoney = query_tbalanceChange.getChangeToltalMoney();//充值总金额
                changeMoney = query_tbalanceChange.getChangeMoney();//实际充值金额
                complimentaryMoney = query_tbalanceChange.getComplimentaryMoney();//奖励金额
                returnMoney = query_tbalanceChange.getRefundMoney();//退款金额
                String originorderNo = query_tbalanceChange.getPayorderNO();

                tBalanceChange.setMemo("1");
                tBalanceChange.setOriginorderNo(originorderNo);
                if (complimentaryMoney.compareTo(tempMoney) == 0) {//不奖励
                    if (changeTotalMoney.subtract(returnMoney).compareTo(i) > -1) {//此单可退金额 >= 退款金额
                        try{
                            tBalanceChange.setChangeToltalMoney(i);//实际变化总金额
                            tBalanceChange.setChangeMoney(i);//变化金额
                            tBalanceChange.setComplimentaryMoney(tempMoney);//奖励金额
                            //1.插入一条退款余额变化记录
                            int insert_Change=  tBalanceChangeService.insertTBalanceChange(tBalanceChange);
                            if (insert_Change==1){
                                try{
                                    //2.修改充值余额变化记录
                                    query_tbalanceChange.setRefundMoney(returnMoney.add(i));
                                    int update_Change = tBalanceChangeService.updateTBalanceChange(query_tbalanceChange);
                                    if (update_Change==1){
                                        try
                                        {//3.修改tmember金额的变化
                                            MoneyOne = tMember.getBalance().subtract(i);//退款金额
                                            MoneyTwo = tMember.getMoney().subtract(i);//总计
                                            MoneyThree = tMember.getComplimentaryMoney().subtract(tempMoney);//奖励
                                            updateTMemberNoAndPhone = tMemberService.updateTMemberNoAndPhone(memberNo, mobilephones, MoneyOne.toString(), points.toString(), MoneyTwo.toString(), MoneyThree.toString());
                                            if (updateTMemberNoAndPhone==1){
                                                return success();
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            return error("修改会员余额错误");
                                        }

                                    }
                                }
                                catch (Exception e)
                                {
                                    return error("修改余额变化错误");
                                }
                            }

                        }
                        catch (Exception e)
                        {
                            return error("插入退款余额变化错误");
                        }
                    } else {
                        tBalanceChange.setChangeToltalMoney(changeTotalMoney.subtract(returnMoney));//实际变化总金额
                        tBalanceChange.setChangeMoney(changeTotalMoney.subtract(returnMoney));
                        tBalanceChange.setComplimentaryMoney(tempMoney);
                        i = i.subtract(changeTotalMoney.subtract(returnMoney));
                        try{
                            //1.插入一条退款余额变化记录
                            int a  = tBalanceChangeService.insertTBalanceChange(tBalanceChange);
                            if (a==1){
                                try
                                {
                                    //2.修改充值余额变化记录
                                    query_tbalanceChange.setRefundMoney(changeTotalMoney);
                                    int b = tBalanceChangeService.updateTBalanceChange(query_tbalanceChange);
                                    if (b==1){
                                        try
                                        {
                                            //3.修改tmember金额的变化
                                            MoneyOne = tMember.getBalance().subtract(changeTotalMoney.subtract(returnMoney));//退款金额
                                            MoneyTwo = tMember.getMoney().subtract(changeTotalMoney.subtract(returnMoney));//总计
                                            MoneyThree = tMember.getComplimentaryMoney().subtract(tempMoney);//奖励
                                            updateTMemberNoAndPhone = tMemberService.updateTMemberNoAndPhone(memberNo, mobilephones, MoneyOne.toString(), points.toString(), MoneyTwo.toString(), MoneyThree.toString());
                                            if (updateTMemberNoAndPhone==1){
                                                continue;
                                            }else {
                                                break;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            return error("修改退款余额错误");
                                        }

                                    }else {
                                        System.out.println("跳出循环");
                                        break;
                                    }
                                }
                                catch (Exception e)
                                {
                                    return error("修改退款余额变化错误");
                                }

                            }else {
                                System.out.println("跳出循环");
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            return error("插入退款余额变化错误");
                        }
                    }
                }else {//含有奖励
                    if (changeTotalMoney.subtract(returnMoney).compareTo(i) > -1) {//此单可退金额 >= 退款金额
                        System.out.println("我进来了11111111111");
                        tBalanceChange.setChangeToltalMoney(i);//实际变化总金额
                        if (complimentaryMoney.subtract(returnMoney).compareTo(i) > -1) {//奖励金额-退款金额>=要退金额  只退奖励
                            System.out.println("我进来了22222222222");
                            onetimeMoney=tempMoney;
                            tBalanceChange.setChangeMoney(onetimeMoney);
                            tBalanceChange.setComplimentaryMoney(i);
                        } else {//奖励金额-退款金额<要退金额
                            if (complimentaryMoney.subtract(returnMoney).compareTo(tempMoney)> -1){//奖励金额-退款金额>=0
                                System.out.println("我进来了33333333333");
                                onetimeMoney=i.subtract(complimentaryMoney.subtract(returnMoney));
                                tBalanceChange.setChangeMoney(onetimeMoney);
                                tBalanceChange.setComplimentaryMoney(complimentaryMoney.subtract(returnMoney));
                            }else {
                                onetimeMoney=i;
                                tBalanceChange.setChangeMoney(i);
                                tBalanceChange.setComplimentaryMoney(tempMoney);
                            }
                        }
                        try
                        {
                            //1.插入一条退款余额变化记录
                            int p = tBalanceChangeService.insertTBalanceChange(tBalanceChange);
                            if (p==1){
                                try{
                                    System.out.println("我进来了444444444444444  错误修改地");
                                    //2.修改充值余额变化记录
                                    query_tbalanceChange.setRefundMoney(returnMoney.add(i));
                                    int c = tBalanceChangeService.updateTBalanceChange(query_tbalanceChange);
                                    if (c==1){
                                        try
                                        {
                                            //3.修改tmember金额的变化
                                            MoneyOne = tMember.getBalance().subtract(onetimeMoney);//退款金额
                                            MoneyTwo = tMember.getMoney().subtract(i);//总计
                                            MoneyThree = tMember.getComplimentaryMoney().subtract(i.subtract(onetimeMoney));//奖励
                                            updateTMemberNoAndPhone = tMemberService.updateTMemberNoAndPhone(memberNo, mobilephones, MoneyOne.toString(), points.toString(), MoneyTwo.toString(), MoneyThree.toString());
                                            if (updateTMemberNoAndPhone==1){
                                                break;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            return error("修改会员余额错误");
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    return error("修改余额变化错误");
                                }
                            }else {
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            return error("插入余额变化错误");
                        }
                    } else {//此单可退金额 <退款金额
                        try{
                            tBalanceChange.setChangeToltalMoney(changeTotalMoney.subtract(returnMoney));//实际变化总金额
                            tBalanceChange.setChangeMoney(changeTotalMoney.subtract(returnMoney));
                            tBalanceChange.setComplimentaryMoney(tempMoney);
                            i = i.subtract(changeTotalMoney.subtract(returnMoney));
                            //1.插入一条退款余额变化记录
                            int w = tBalanceChangeService.insertTBalanceChange(tBalanceChange);
                            if (w==1){
                                try{
                                    System.out.println("我进来了5555555555");
                                    //2.修改充值余额变化记录
                                    query_tbalanceChange.setRefundMoney(changeTotalMoney);
                                    int q = tBalanceChangeService.updateTBalanceChange(query_tbalanceChange);
                                    if (q==1){
                                        try{
                                            System.out.println("我进来了6666666666666666666666");
                                            //3.修改tmember金额的变化
                                            MoneyOne = tMember.getBalance().subtract(changeTotalMoney.subtract(returnMoney));//退款金额
                                            MoneyTwo = tMember.getMoney().subtract(changeTotalMoney.subtract(returnMoney));//总计
                                            MoneyThree = tMember.getComplimentaryMoney().subtract(tempMoney);//奖励
                                            updateTMemberNoAndPhone = tMemberService.updateTMemberNoAndPhone(memberNo, mobilephones, MoneyOne.toString(), points.toString(), MoneyTwo.toString(), MoneyThree.toString());
                                            if (updateTMemberNoAndPhone==1){
                                                continue;
                                            }
                                        }
                                        catch (Exception e)
                                        {
                                            return error("修改会员余额错误");
                                        }
                                    }else {
                                        System.out.println("跳出循环");
                                        break;
                                    }
                                }
                                catch (Exception e)
                                {
                                    return error("修改余额变化错误");
                                }
                            }else {
                                System.out.println("跳出循环");
                                break;
                            }
                        }
                        catch (Exception e)
                        {
                            return error("插入余额变化错误");
                        }
                    }
                }
            }
        }
        return toAjax(updateTMemberNoAndPhone);
    }

    /**
     * 校验部门名称
     */
    @PostMapping("/checkTMemberIslossUnique")
    @ResponseBody
    public String checkTMemberNameUnique(TMember tMember) {
        SysUser user = ShiroUtils.getSysUser();
        //user.getLoginName();//登录名称
        SysUser sysUser = userService.selectUserByLoginName(user.getLoginName());
        tMember.setCompanyid(sysUser.getDeptId());
        System.out.println(tMember);
        String s = tMemberService.checkDeptNameUnique(tMember);
        return s;
    }

    /**
     * 校验卡号
     */
    @PostMapping("/checkCardCount")
    @ResponseBody
    public String checkCardCount(TMember tMember) {
        //System.out.println("-------------------"+tMember.getMemberTypeId());
        return tMemberService.checkCardCount(tMember);
    }

    /**
     * 修改会员
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap) {
        TMember tMember = tMemberService.selectTMemberById(id);
        mmap.put("tMember", tMember);
        return prefix + "/edit";
    }

    /**
     * 修改保存会员
     */
    @RequiresPermissions("system:recharge:edit")
    @Log(title = "会员", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave(@Param("memberNoOne") String memberNoOne, @Param("memberNoTwo") String memberNoTwo) {
        /*首先根据id获取要插入的信息*/
        TMember tMemberById = tMemberService.selectTMemberById(Long.valueOf(memberNoOne));
        System.out.println(tMemberById);
        /*在插入的过程中设置要修改的卡号*/
        tMemberById.setId(null);
        tMemberById.setMemberNo(memberNoTwo);
        tMemberById.setIsloss(0);
        tMemberService.insertTMember(tMemberById);

        TMember tMember = new TMember();
        tMember.setMemberNo(memberNoOne);
        BigDecimal money = new BigDecimal(0);
        /*保留原卡信息并现有金额和奖励金额清零*/
        tMember.setBalance(money);
        tMember.setComplimentaryMoney(money);
        int updateTMember = tMemberService.updateTMemberByMemberNo(tMember);
        TCardReplacement tCardReplacement = new TCardReplacement();
        tCardReplacement.setCardnoOld(memberNoOne);
        tCardReplacement.setCardnoNew(memberNoTwo);
        SysUser user = ShiroUtils.getSysUser();
        tCardReplacement.setOperator(user.getUserName());
        tCardReplacement.setOperateTime(DateUtilsStr.dateStrUtils(DateUtils.getTime(), DateUtilsStr.DATE_TIME_PATTERN));
        TMember tMemberCompanyid = tMemberService.selectTMemberById(Long.valueOf(memberNoOne));
        tCardReplacement.setCompanyid(tMemberCompanyid.getCompanyid());
        /*插入一条补卡记录*/
        tCardReplacementService.insertTCardReplacement(tCardReplacement);
        tBalanceChangeService.updateTBalanceChangeByMemberNo(memberNoTwo, memberNoOne);
        return toAjax(updateTMember);
    }

}
