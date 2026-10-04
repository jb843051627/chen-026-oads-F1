package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 届期催办与巡查条目对象 t_oads_notice_item
 *
 * @author fuce
 * @date 2026-09-12
 */
@TableName("t_oads_notice_item")
@ApiModel(value = "TOadsNoticeItem", description = "届期催办与巡查条目")
public class TOadsNoticeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 届期催办与巡查条目代号 */
    @TableField("item_no")
    @ApiModelProperty(value = "届期催办与巡查条目代号")
    private String itemNo;

    /** 该发催办那一一日（落库为 datetime） */
    @TableField("due_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "该发催办那一一日（落库为 datetime）")
    private Date dueAt;

    /** 最多许提前几日 */
    @TableField("amount")
    @ApiModelProperty(value = "最多许提前几日")
    private BigDecimal amount;

    /** 事由与所对点位的记要 */
    @TableField("content")
    @ApiModelProperty(value = "事由与所对点位的记要")
    private String content;

    /** 所对点位码（挂点位底册） */
    @TableField("spot_no")
    @ApiModelProperty(value = "所对点位码（挂点位底册）")
    private String spotNo;

    /** 路段目录代号（并拨键一） */
    @TableField("area_no")
    @ApiModelProperty(value = "路段目录代号（并拨键一）")
    private String areaNo;

    /** 门牌段（并拨键二） */
    @TableField("door_seg")
    @ApiModelProperty(value = "门牌段（并拨键二）")
    private String doorSeg;

    /** 联系栏 */
    @TableField("contact")
    @ApiModelProperty(value = "联系栏")
    private String contact;

    /** 条目情形 0搁着(未启动) 1已发出(已派发) 2发不成(派不出) */
    @TableField("status")
    @ApiModelProperty(value = "条目情形 0搁着(未启动) 1已发出(已派发) 2发不成(派不出)")
    private Integer status;

    /** 派发那一刻（两边记号之一） */
    @TableField("sent_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "派发那一刻（两边记号之一）")
    private Date sentAt;

    /** 接手的岗位（两边记号之一） */
    @TableField("sent_post")
    @ApiModelProperty(value = "接手的岗位（两边记号之一）")
    private String sentPost;

    /** 卡住的那一步 */
    @TableField("fail_step")
    @ApiModelProperty(value = "卡住的那一步")
    private String failStep;

    /** 派不出原因（毛病出在哪一格） */
    @TableField("fail_reason")
    @ApiModelProperty(value = "派不出原因（毛病出在哪一格）")
    private String failReason;

    /** 本轮轮次码（三个数只讲本次这一轮） */
    @TableField("round_no")
    @ApiModelProperty(value = "本轮轮次码（三个数只讲本次这一轮）")
    private String roundNo;

    /** 删除标记 0正常 1删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记 0正常 1删除")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public Date getDueAt() {
        return dueAt;
    }

    public void setDueAt(Date dueAt) {
        this.dueAt = dueAt;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSpotNo() {
        return spotNo;
    }

    public void setSpotNo(String spotNo) {
        this.spotNo = spotNo;
    }

    public String getAreaNo() {
        return areaNo;
    }

    public void setAreaNo(String areaNo) {
        this.areaNo = areaNo;
    }

    public String getDoorSeg() {
        return doorSeg;
    }

    public void setDoorSeg(String doorSeg) {
        this.doorSeg = doorSeg;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getSentAt() {
        return sentAt;
    }

    public void setSentAt(Date sentAt) {
        this.sentAt = sentAt;
    }

    public String getSentPost() {
        return sentPost;
    }

    public void setSentPost(String sentPost) {
        this.sentPost = sentPost;
    }

    public String getFailStep() {
        return failStep;
    }

    public void setFailStep(String failStep) {
        this.failStep = failStep;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public String getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(String roundNo) {
        this.roundNo = roundNo;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
