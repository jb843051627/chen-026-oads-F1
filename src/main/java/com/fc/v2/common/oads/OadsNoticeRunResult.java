package com.fc.v2.common.oads;

import java.io.Serializable;

/**
 * 一轮夜班的交代（规则九）：三个数只讲本次这一轮，历史不并进来；
 * 页面看到的与库里数出的出自同一次计算——就是这一个对象。
 */
public class OadsNoticeRunResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 本轮有没有动过清单 */
    private boolean changed;

    /** 回给按下执行键的那句话 */
    private String message;

    /** 尚未派发的还有几行（收尾时回清单数出） */
    private int pending;

    /** 本轮派发几行 */
    private int dispatched;

    /** 本轮记成派不出几行 */
    private int failed;

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

    public int getPending() {
        return pending;
    }

    public void setPending(int pending) {
        this.pending = pending;
    }

    public int getDispatched() {
        return dispatched;
    }

    public void setDispatched(int dispatched) {
        this.dispatched = dispatched;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        return "OadsNoticeRunResult{changed=" + changed
                + ", message='" + message + '\''
                + ", pending=" + pending
                + ", dispatched=" + dispatched
                + ", failed=" + failed + '}';
    }
}
