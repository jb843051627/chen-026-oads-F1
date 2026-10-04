package com.fc.v2.model.custom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 届期催办与巡查：一轮夜跑的交代。
 * 三个数只讲本次这一轮，历史不并进来；页面看到的与库里数出的出自同一次计算。
 *
 * @author fuce
 * @date 2026-10-04
 */
@ApiModel(value = "OadsNoticeRunResult", description = "届期催办与巡查一轮夜跑结果")
public class OadsNoticeRunResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 窗口之外按下执行键的应答原话（连按两次一字不差） */
    public static final String MSG_NO_CHANGE = "本次未改动清单";
    /** 一行也没挑中的应答原话：平常日子，不必当事故上报 */
    public static final String MSG_NO_DUE = "本轮无到期";

    /** 本轮轮次码 */
    @ApiModelProperty(value = "本轮轮次码")
    private String roundNo;

    /** 清单是否被本轮改动 */
    @ApiModelProperty(value = "清单是否被本轮改动")
    private boolean changed;

    /** 应答原话 */
    @ApiModelProperty(value = "应答原话")
    private String message;

    /** 尚未派发的还有几行 */
    @ApiModelProperty(value = "尚未派发的还有几行")
    private long pendingCount;

    /** 本轮派发几行 */
    @ApiModelProperty(value = "本轮派发几行")
    private long dispatchedCount;

    /** 记成派不出几行 */
    @ApiModelProperty(value = "记成派不出几行")
    private long failedCount;

    /** 窗口之外：只应答，一行都不许改动 */
    public static OadsNoticeRunResult noChange() {
        OadsNoticeRunResult r = new OadsNoticeRunResult();
        r.setChanged(false);
        r.setMessage(MSG_NO_CHANGE);
        return r;
    }

    /** 一行也没挑中：回一句本轮无到期即收工 */
    public static OadsNoticeRunResult noDue(String roundNo) {
        OadsNoticeRunResult r = new OadsNoticeRunResult();
        r.setRoundNo(roundNo);
        r.setChanged(false);
        r.setMessage(MSG_NO_DUE);
        return r;
    }

    /** 正常一轮：回清单逐行数出的三个数 */
    public static OadsNoticeRunResult of(String roundNo, long pending, long dispatched, long failed) {
        OadsNoticeRunResult r = new OadsNoticeRunResult();
        r.setRoundNo(roundNo);
        r.setChanged(dispatched > 0 || failed > 0);
        r.setPendingCount(pending);
        r.setDispatchedCount(dispatched);
        r.setFailedCount(failed);
        r.setMessage("尚未派发" + pending + "行，本轮派发" + dispatched + "行，记成派不出" + failed + "行");
        return r;
    }

    public String getRoundNo() {
        return roundNo;
    }

    public void setRoundNo(String roundNo) {
        this.roundNo = roundNo;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public long getDispatchedCount() {
        return dispatchedCount;
    }

    public void setDispatchedCount(long dispatchedCount) {
        this.dispatchedCount = dispatchedCount;
    }

    public long getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(long failedCount) {
        this.failedCount = failedCount;
    }
}
