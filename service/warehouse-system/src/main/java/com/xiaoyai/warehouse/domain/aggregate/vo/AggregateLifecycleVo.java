package com.xiaoyai.warehouse.domain.aggregate.vo;

import com.xiaoyai.warehouse.domain.aggregate.AggregateEvent;
import com.xiaoyai.warehouse.domain.aggregate.AggregateRfidIdentity;
import com.xiaoyai.warehouse.domain.aggregate.AggregateSubjectBindRecord;

import java.util.List;

/**
 * RFID生命周期时间线
 */
public class AggregateLifecycleVo {
    private AggregateRfidIdentity identity;
    private List<AggregateEvent> events;
    private List<AggregateSubjectBindRecord> bindRecords;

    public AggregateRfidIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(AggregateRfidIdentity identity) {
        this.identity = identity;
    }

    public List<AggregateEvent> getEvents() {
        return events;
    }

    public void setEvents(List<AggregateEvent> events) {
        this.events = events;
    }

    public List<AggregateSubjectBindRecord> getBindRecords() {
        return bindRecords;
    }

    public void setBindRecords(List<AggregateSubjectBindRecord> bindRecords) {
        this.bindRecords = bindRecords;
    }
}
