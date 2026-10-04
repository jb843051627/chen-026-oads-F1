package com.fc.v2.common.oads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 夜间轮次的公用口径配置（application.yml 的 oads.notice 一节）。
 *
 * <p>可提前几日这个数只认清单本行写定的那一格（规则四），这里不设第二条来源；
 * 这里只配两处真正公用的数：哪些日子办不成事、岗位每季派发定额。</p>
 */
@Component
@ConfigurationProperties(prefix = "oads.notice")
public class OadsNoticeProperties {

    /** 法定节假日（yyyy-MM-dd），数日子时不算 */
    private List<String> holidays = new ArrayList<>();

    /** 市里下文停办的日子（yyyy-MM-dd），数日子时不算 */
    private List<String> suspensions = new ArrayList<>();

    /** 岗位每季派发定额：未单列的岗位一律用这个数 */
    private int quotaDefault = 500;

    /** 分岗位定额：岗位代号 -&gt; 每季件数 */
    private Map<String, Integer> quotaByPost = new HashMap<>();

    public List<String> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<String> holidays) {
        this.holidays = holidays;
    }

    public List<String> getSuspensions() {
        return suspensions;
    }

    public void setSuspensions(List<String> suspensions) {
        this.suspensions = suspensions;
    }

    public int getQuotaDefault() {
        return quotaDefault;
    }

    public void setQuotaDefault(int quotaDefault) {
        this.quotaDefault = quotaDefault;
    }

    public Map<String, Integer> getQuotaByPost() {
        return quotaByPost;
    }

    public void setQuotaByPost(Map<String, Integer> quotaByPost) {
        this.quotaByPost = quotaByPost;
    }
}
