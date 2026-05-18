package com.codingapi.flow.infra.jpa.projection;

public interface FlowRecordSummary {

    Long getId();

    Long getWorkRuntimeId();

    String getWorkTitle();

    String getWorkCode();

    String getNodeId();

    String getNodeType();

    String getNodeName();

    Long getFromId();

    Long getParentId();

    String getTitle();

    Long getReadTime();

    String getProcessId();

    String getActionId();

    String getActionType();

    String getActionName();

    String getAdvice();

    String getSignKey();

    Long getCurrentOperatorId();

    String getCurrentOperatorName();

    Long getSubmitOperatorId();

    String getSubmitOperatorName();

    Long getForwardOperatorId();

    String getForwardOperatorName();

    String getReturnNodeId();

    Integer getNodeOrder();

    Boolean getHidden();

    Boolean getRevoked();

    Boolean getNotify();

    Integer getRecordState();

    Integer getFlowState();

    Long getUpdateTime();

    Long getCreateTime();

    Long getFinishTime();

    Boolean getReadable();

    Long getCreateOperatorId();

    String getCreateOperatorName();

    String getErrMessage();

    Long getTimeoutTime();

    Boolean getMergeable();

    Long getInterferedOperatorId();

    String getInterferedOperatorName();

    Long getDelegateId();

    String getParallelId();

    String getParallelBranchNodeId();

    Integer getParallelBranchTotal();
}
