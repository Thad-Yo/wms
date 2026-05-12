package com.xiaoyai.warehouse.domain.aggregate.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * APP骨料溯源详情
 */
public class AggregateAppTraceVo {
    private String rfidCode;
    private String subjectName;
    private String subjectCode;
    private String subjectType;
    private String bindStatus;
    private String boneName;
    private String batchNo;
    private String templateName;
    private String bindTime;
    private String latestLocation;
    private List<FieldItem> fields = new ArrayList<>();
    private List<TimelineItem> timeline = new ArrayList<>();

    public String getRfidCode() {
        return rfidCode;
    }

    public void setRfidCode(String rfidCode) {
        this.rfidCode = rfidCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getBindStatus() {
        return bindStatus;
    }

    public void setBindStatus(String bindStatus) {
        this.bindStatus = bindStatus;
    }

    public String getBoneName() {
        return boneName;
    }

    public void setBoneName(String boneName) {
        this.boneName = boneName;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getBindTime() {
        return bindTime;
    }

    public void setBindTime(String bindTime) {
        this.bindTime = bindTime;
    }

    public String getLatestLocation() {
        return latestLocation;
    }

    public void setLatestLocation(String latestLocation) {
        this.latestLocation = latestLocation;
    }

    public List<FieldItem> getFields() {
        return fields;
    }

    public void setFields(List<FieldItem> fields) {
        this.fields = fields;
    }

    public List<TimelineItem> getTimeline() {
        return timeline;
    }

    public void setTimeline(List<TimelineItem> timeline) {
        this.timeline = timeline;
    }

    public static class FieldItem {
        private String label;
        private String value;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class TimelineItem {
        private String title;
        private String stage;
        private String desc;
        private String operator;
        private String time;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getStage() {
            return stage;
        }

        public void setStage(String stage) {
            this.stage = stage;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
