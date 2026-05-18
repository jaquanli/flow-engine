package com.codingapi.flow.infra.jpa;

import com.codingapi.flow.infra.entity.FlowRecordEntity;
import com.codingapi.flow.infra.jpa.projection.FlowRecordSummary;
import com.codingapi.springboot.fast.jpa.repository.FastRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FlowRecordEntityRepository extends FastRepository<FlowRecordEntity, Long> {

    FlowRecordEntity getFlowRecordEntityById(long id);

    @Query("""
            select r.id as id,
                   r.workRuntimeId as workRuntimeId,
                   r.workTitle as workTitle,
                   r.workCode as workCode,
                   r.nodeId as nodeId,
                   r.nodeType as nodeType,
                   r.nodeName as nodeName,
                   r.fromId as fromId,
                   r.parentId as parentId,
                   r.title as title,
                   r.readTime as readTime,
                   r.processId as processId,
                   r.actionId as actionId,
                   r.actionType as actionType,
                   r.actionName as actionName,
                   r.advice as advice,
                   r.signKey as signKey,
                   r.currentOperatorId as currentOperatorId,
                   r.currentOperatorName as currentOperatorName,
                   r.submitOperatorId as submitOperatorId,
                   r.submitOperatorName as submitOperatorName,
                   r.forwardOperatorId as forwardOperatorId,
                   r.forwardOperatorName as forwardOperatorName,
                   r.returnNodeId as returnNodeId,
                   r.nodeOrder as nodeOrder,
                   r.hidden as hidden,
                   r.revoked as revoked,
                   r.notify as notify,
                   r.recordState as recordState,
                   r.flowState as flowState,
                   r.updateTime as updateTime,
                   r.createTime as createTime,
                   r.finishTime as finishTime,
                   r.readable as readable,
                   r.createOperatorId as createOperatorId,
                   r.createOperatorName as createOperatorName,
                   r.errMessage as errMessage,
                   r.timeoutTime as timeoutTime,
                   r.mergeable as mergeable,
                   r.interferedOperatorId as interferedOperatorId,
                   r.interferedOperatorName as interferedOperatorName,
                   r.delegateId as delegateId,
                   r.parallelId as parallelId,
                   r.parallelBranchNodeId as parallelBranchNodeId,
                   r.parallelBranchTotal as parallelBranchTotal
              from FlowRecordEntity r
             where r.id = ?1
            """)
    FlowRecordSummary getProcessNodeRecordById(long id);

    @Query("from FlowRecordEntity r where r.revoked = false")
    Page<FlowRecordEntity> findAllFlowRecords(PageRequest pageRequest);

    @Query("from FlowRecordEntity r where r.id in ?1")
    List<FlowRecordEntity> findByIds(List<Long> ids);

    @Query("from FlowRecordEntity r where r.processId = ?1")
    List<FlowRecordEntity> findProcessIdRecords(String processId);

    @Query("""
            select r.id as id,
                   r.workRuntimeId as workRuntimeId,
                   r.workTitle as workTitle,
                   r.workCode as workCode,
                   r.nodeId as nodeId,
                   r.nodeType as nodeType,
                   r.nodeName as nodeName,
                   r.fromId as fromId,
                   r.parentId as parentId,
                   r.title as title,
                   r.readTime as readTime,
                   r.processId as processId,
                   r.actionId as actionId,
                   r.actionType as actionType,
                   r.actionName as actionName,
                   r.advice as advice,
                   r.signKey as signKey,
                   r.currentOperatorId as currentOperatorId,
                   r.currentOperatorName as currentOperatorName,
                   r.submitOperatorId as submitOperatorId,
                   r.submitOperatorName as submitOperatorName,
                   r.forwardOperatorId as forwardOperatorId,
                   r.forwardOperatorName as forwardOperatorName,
                   r.returnNodeId as returnNodeId,
                   r.nodeOrder as nodeOrder,
                   r.hidden as hidden,
                   r.revoked as revoked,
                   r.notify as notify,
                   r.recordState as recordState,
                   r.flowState as flowState,
                   r.updateTime as updateTime,
                   r.createTime as createTime,
                   r.finishTime as finishTime,
                   r.readable as readable,
                   r.createOperatorId as createOperatorId,
                   r.createOperatorName as createOperatorName,
                   r.errMessage as errMessage,
                   r.timeoutTime as timeoutTime,
                   r.mergeable as mergeable,
                   r.interferedOperatorId as interferedOperatorId,
                   r.interferedOperatorName as interferedOperatorName,
                   r.delegateId as delegateId,
                   r.parallelId as parallelId,
                   r.parallelBranchNodeId as parallelBranchNodeId,
                   r.parallelBranchTotal as parallelBranchTotal
              from FlowRecordEntity r
             where r.processId = ?1
            """)
    List<FlowRecordSummary> findProcessNodeRecords(String processId);

    @Query("from FlowRecordEntity r where r.fromId = ?1 and r.nodeId =?2 and r.revoked = false")
    List<FlowRecordEntity> findCurrentNodeRecords(long fromId, String nodeId);

    @Query("from FlowRecordEntity r where r.processId = ?1 and (r.recordState = 0 and r.flowState = 0 and r.hidden=false and r.revoked = false)")
    List<FlowRecordEntity> findTodoRecords(String processId);

    @Query("from FlowRecordEntity r where r.processId = ?1 and r.fromId >=?2 and r.hidden=false and r.revoked = false")
    List<FlowRecordEntity> findAfterRecords(String processId, long fromId);

    @Query("from FlowRecordEntity r where r.processId = ?1 and r.id < ?2 and r.hidden=false and r.revoked = false")
    List<FlowRecordEntity> findBeforeRecords(String processId, long id);

    @Query("from FlowRecordEntity r where r.currentOperatorId = ?1 and (r.recordState = 0 and r.flowState = 0 and r.hidden=false and r.revoked = false)")
    Page<FlowRecordEntity> findTodoRecordPage(long operatorId, PageRequest pageRequest);

    @Query("from FlowRecordEntity r where r.currentOperatorId = ?1 and (r.recordState = 1 and r.hidden=false and r.revoked = false) ")
    Page<FlowRecordEntity> findDoneRecordPage(long operatorId, PageRequest pageRequest);

    @Query("from FlowRecordEntity r where r.currentOperatorId = ?1 and r.notify = true and r.hidden=false and r.revoked = false ")
    Page<FlowRecordEntity> findNotifyRecordPage(long operatorId, PageRequest pageRequest);


}
