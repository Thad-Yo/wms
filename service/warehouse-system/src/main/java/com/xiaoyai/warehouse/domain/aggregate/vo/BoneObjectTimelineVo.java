package com.xiaoyai.warehouse.domain.aggregate.vo;

import com.xiaoyai.warehouse.domain.aggregate.BoneObject;
import com.xiaoyai.warehouse.domain.aggregate.BoneObjectEvent;

import java.util.List;

/**
 * 对象时间线
 */
public class BoneObjectTimelineVo {
    private BoneObject object;
    private List<BoneObjectEvent> events;

    public BoneObject getObject() {
        return object;
    }

    public void setObject(BoneObject object) {
        this.object = object;
    }

    public List<BoneObjectEvent> getEvents() {
        return events;
    }

    public void setEvents(List<BoneObjectEvent> events) {
        this.events = events;
    }
}
