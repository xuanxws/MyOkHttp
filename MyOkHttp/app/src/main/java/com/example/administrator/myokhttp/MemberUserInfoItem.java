package com.example.administrator.myokhttp;

import java.io.Serializable;

/**
 * 账户基本信息实体类【5.7.3新增】
 * Created by xuws on 2017/5/22.
 */

public class MemberUserInfoItem implements Serializable {

    private static final long serialVersionUID = -6130267928964085412L;

    private String user_id = "";//用户id

    private String truename = "";//真实姓名

    private String card_type = "";//证件类型，1：身份证 2：港澳通行证 3：护照

    private String card = "";//证件号

    private String show_card = "";//展示的证件号，隐私处理

    private String sex = "";//性别，0男1女

    private String birthday = "";//出生日期

    private String verify_status = "";//实名认证状态，0未认证，1已认证，2审核中(实名认证)，3审核中(若用户提交“修改申请”且审核中)，4未认证（审核不通过）；

    private String mobile = "";//手机

    private String show_mobile = "";//展示的手机号，隐私处理

    private String account_status = "";//帐号信息申请修改状态，0未认证，1已认证，2审核中(实名认证)，3审核中（申请修改）4未认证（审核不通过）

    private String verify_reason = "";//审核不通过的理由

    private String relation = "";

    private String is_default = "";

    private String member_id = "";

    private String from;//5.7.3来源，"ask"为咨询者

    private String avatar = "";//头像地址

    private String account_reason = "";//账户修改审核不通过的理由

    public String getAccount_reason() {
        return null == account_reason ? "" : account_reason;
    }

    public void setAccount_reason(String account_reason) {
        this.account_reason = account_reason;
    }

    public String getAvatar() {
        return null == avatar ? "" : avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getFrom() {
        return null == from ? "" : from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMember_id() {
        return null == member_id ? "" : member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public String getIs_default() {
        return null == is_default ? "" : is_default;
    }

    public void setIs_default(String is_default) {
        this.is_default = is_default;
    }

    public String getRelation() {
        return null == relation ? "" : relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public String getVerify_reason() {
        return null == verify_reason ? "" : verify_reason;
    }

    public void setVerify_reason(String verify_reason) {
        this.verify_reason = verify_reason;
    }

    public String getUser_id() {
        return null == user_id ? "" : user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getTruename() {
        return null == truename ? "" : truename;
    }

    public void setTruename(String truename) {
        this.truename = truename;
    }

    public String getCard_type() {
        return null == card_type ? "" : card_type;
    }

    public void setCard_type(String card_type) {
        this.card_type = card_type;
    }

    public String getCard() {
        return null == card ? "" : card;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public String getShow_card() {
        return null == show_card ? "" : show_card;
    }

    public void setShow_card(String show_card) {
        this.show_card = show_card;
    }

    public String getSex() {
        return null == sex ? "" : sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getBirthday() {
        return null == birthday ? "" : birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getVerify_status() {
        return null == verify_status ? "" : verify_status;
    }

    public void setVerify_status(String verify_status) {
        this.verify_status = verify_status;
    }

    public String getMobile() {
        return null == mobile ? "" : mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getShow_mobile() {
        return null == show_mobile ? "" : show_mobile;
    }

    public void setShow_mobile(String show_mobile) {
        this.show_mobile = show_mobile;
    }

    public String getAccount_status() {
        return null == account_status ? "" : account_status;
    }

    public void setAccount_status(String account_status) {
        this.account_status = account_status;
    }
}
