package com.codingapi.flow.infra.convert;

import com.codingapi.flow.infra.entity.FlowRecordEntity;
import com.codingapi.flow.infra.entity.convert.MapConvertor;
import com.codingapi.flow.infra.jpa.projection.FlowRecordSummary;
import com.codingapi.flow.record.FlowRecord;

public class FlowRecordConvertor {

    private final static MapConvertor mapConvertor = new MapConvertor();

    public static FlowRecord convert(FlowRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FlowRecord(entity.getId(),
                entity.getWorkRuntimeId(),
                entity.getWorkTitle(),
                entity.getWorkCode(),
                entity.getNodeId(),
                entity.getNodeType(),
                entity.getNodeName(),
                entity.getFromId(),
                entity.getParentId(),
                mapConvertor.convertToEntityAttribute(entity.getFormData()),
                entity.getTitle(),
                entity.getReadTime(),
                entity.getProcessId(),
                entity.getActionId(),
                entity.getActionType(),
                entity.getActionName(),
                entity.getAdvice(),
                entity.getSignKey(),
                entity.getCurrentOperatorId(),
                entity.getCurrentOperatorName(),
                entity.getSubmitOperatorId(),
                entity.getSubmitOperatorName(),
                entity.getForwardOperatorId(),
                entity.getForwardOperatorName(),
                entity.getReturnNodeId(),
                entity.getNodeOrder(),
                entity.getHidden(),
                entity.getRevoked(),
                entity.getNotify(),
                entity.getRecordState(),
                entity.getFlowState(),
                entity.getUpdateTime(),
                entity.getCreateTime(),
                entity.getFinishTime(),
                entity.getReadable(),
                entity.getCreateOperatorId(),
                entity.getCreateOperatorName(),
                entity.getErrMessage(),
                entity.getTimeoutTime(),
                entity.getMergeable(),
                entity.getInterferedOperatorId(),
                entity.getInterferedOperatorName(),
                entity.getDelegateId(),
                entity.getParallelId(),
                entity.getParallelBranchNodeId(),
                entity.getParallelBranchTotal());
    }

    public static FlowRecord convert(FlowRecordSummary summary) {
        if (summary == null) {
            return null;
        }
        return new FlowRecord(number(summary.getId()),
                number(summary.getWorkRuntimeId()),
                summary.getWorkTitle(),
                summary.getWorkCode(),
                summary.getNodeId(),
                summary.getNodeType(),
                summary.getNodeName(),
                number(summary.getFromId()),
                number(summary.getParentId()),
                null,
                summary.getTitle(),
                number(summary.getReadTime()),
                summary.getProcessId(),
                summary.getActionId(),
                summary.getActionType(),
                summary.getActionName(),
                summary.getAdvice(),
                summary.getSignKey(),
                number(summary.getCurrentOperatorId()),
                summary.getCurrentOperatorName(),
                number(summary.getSubmitOperatorId()),
                summary.getSubmitOperatorName(),
                number(summary.getForwardOperatorId()),
                summary.getForwardOperatorName(),
                summary.getReturnNodeId(),
                number(summary.getNodeOrder()),
                flag(summary.getHidden()),
                flag(summary.getRevoked()),
                flag(summary.getNotify()),
                number(summary.getRecordState()),
                number(summary.getFlowState()),
                number(summary.getUpdateTime()),
                number(summary.getCreateTime()),
                number(summary.getFinishTime()),
                flag(summary.getReadable()),
                number(summary.getCreateOperatorId()),
                summary.getCreateOperatorName(),
                summary.getErrMessage(),
                number(summary.getTimeoutTime()),
                flag(summary.getMergeable()),
                number(summary.getInterferedOperatorId()),
                summary.getInterferedOperatorName(),
                number(summary.getDelegateId()),
                summary.getParallelId(),
                summary.getParallelBranchNodeId(),
                number(summary.getParallelBranchTotal()));
    }

    private static long number(Long value) {
        return value == null ? 0 : value;
    }

    private static int number(Integer value) {
        return value == null ? 0 : value;
    }

    private static boolean flag(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    public static FlowRecordEntity convert(FlowRecord record) {
        if (record == null) {
            return null;
        }
        FlowRecordEntity entity = new FlowRecordEntity();
        if(record.getId()>0) {
            entity.setId(record.getId());
        }
        entity.setWorkRuntimeId(record.getWorkRuntimeId());
        entity.setWorkTitle(record.getWorkTitle());
        entity.setWorkCode(record.getWorkCode());
        entity.setNodeId(record.getNodeId());
        entity.setNodeType(record.getNodeType());
        entity.setNodeName(record.getNodeName());
        entity.setFromId(record.getFromId());
        entity.setParentId(record.getParentId());
        entity.setFormData(mapConvertor.convertToDatabaseColumn(record.getFormData()));
        entity.setTitle(record.getTitle());
        entity.setReadTime(record.getReadTime());
        entity.setProcessId(record.getProcessId());
        entity.setActionId(record.getActionId());
        entity.setActionType(record.getActionType());
        entity.setActionName(record.getActionName());
        entity.setAdvice(record.getAdvice());
        entity.setSignKey(record.getSignKey());
        entity.setCurrentOperatorId(record.getCurrentOperatorId());
        entity.setCurrentOperatorName(record.getCurrentOperatorName());
        entity.setSubmitOperatorId(record.getSubmitOperatorId());
        entity.setSubmitOperatorName(record.getSubmitOperatorName());
        entity.setForwardOperatorId(record.getForwardOperatorId());
        entity.setForwardOperatorName(record.getForwardOperatorName());
        entity.setReturnNodeId(record.getReturnNodeId());
        entity.setNodeOrder(record.getNodeOrder());
        entity.setHidden(record.isHidden());
        entity.setRevoked(record.isRevoked());
        entity.setNotify(record.isNotify());
        entity.setRecordState(record.getRecordState());
        entity.setFlowState(record.getFlowState());
        entity.setUpdateTime(record.getUpdateTime());
        entity.setCreateTime(record.getCreateTime());
        entity.setFinishTime(record.getFinishTime());
        entity.setReadable(record.isReadable());
        entity.setCreateOperatorId(record.getCreateOperatorId());
        entity.setCreateOperatorName(record.getCreateOperatorName());
        entity.setErrMessage(record.getErrMessage());
        entity.setTimeoutTime(record.getTimeoutTime());
        entity.setMergeable(record.isMergeable());
        entity.setInterferedOperatorId(record.getInterferedOperatorId());
        entity.setInterferedOperatorName(record.getInterferedOperatorName());
        entity.setDelegateId(record.getDelegateId());
        entity.setParallelId(record.getParallelId());
        entity.setParallelBranchNodeId(record.getParallelBranchNodeId());
        entity.setParallelBranchTotal(record.getParallelBranchTotal());
        return entity;
    }


}
