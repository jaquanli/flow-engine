package com.codingapi.flow.service;

import com.codingapi.flow.record.FlowRecord;
import com.codingapi.flow.record.FlowTodoMerge;
import com.codingapi.flow.record.FlowTodoRecord;
import com.codingapi.flow.repository.FlowRecordRepository;
import com.codingapi.flow.repository.FlowTodoMergeRepository;
import com.codingapi.flow.repository.FlowTodoRecordRepository;
import lombok.AllArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *  流程记录服务对象
 */
@AllArgsConstructor
public class FlowRecordService {

    private static final int PROCESS_NODE_CACHE_SIZE = 1024;

    private final FlowTodoRecordRepository flowTodoRecordRepository;
    private final FlowTodoMergeRepository flowTodoMergeRepository;
    private final FlowRecordRepository flowRecordRepository;
    private final Map<Long, FlowRecord> processNodeRecordCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, FlowRecord> eldest) {
            return size() > PROCESS_NODE_CACHE_SIZE;
        }
    };
    private final Map<String, List<FlowRecord>> processNodeRecordsCache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<FlowRecord>> eldest) {
            return size() > PROCESS_NODE_CACHE_SIZE;
        }
    };


    /**
     * 保存流程记录
     * @param flowRecords 流程记录列表
     */
    public void saveFlowRecords(List<FlowRecord> flowRecords){
        FlowRecordSaveService flowRecordSaveService = new FlowRecordSaveService(flowRecords);
        flowRecordSaveService.registerRepositories(this.flowTodoRecordRepository,this.flowTodoMergeRepository,this.flowRecordRepository);
        flowRecordSaveService.saveAll();
        this.evictProcessNodeRecords(flowRecords);
    }

    /**
     * 保存流程记录
     * @param flowRecord 流程记录
     */
    public void saveFlowRecord(FlowRecord flowRecord) {
        FlowRecordSaveService flowRecordSaveService = new FlowRecordSaveService(flowRecord);
        flowRecordSaveService.registerRepositories(this.flowTodoRecordRepository,this.flowTodoMergeRepository,this.flowRecordRepository);
        flowRecordSaveService.saveAll();
        this.evictProcessNodeRecord(flowRecord);
    }


    /**
     * 获取流程的合并记录
     * @param mergeKey 流程合并key
     * @return 流程合并记录
     */
    public List<FlowRecord> getMergeRecord(String mergeKey){
        FlowTodoRecord todoRecord = flowTodoRecordRepository.getByTodoKey(mergeKey);
        List<FlowTodoMerge> todoMerges = flowTodoMergeRepository.findByTodoId(todoRecord.getId());
        return this.findFlowRecordByIds(todoMerges.stream().map(FlowTodoMerge::getRecordId).toList());
    }


    /**
     * 获取流程记录
     * @param id 流程id
     * @return 流程记录
     */
    public FlowRecord getFlowRecord(long id){
        return flowRecordRepository.get(id);
    }

    /**
     * 获取流程节点展示所需的轻量流程记录
     *
     * @param id 流程id
     * @return 流程记录
     */
    public FlowRecord getProcessNodeRecord(long id) {
        synchronized (processNodeRecordCache) {
            FlowRecord flowRecord = processNodeRecordCache.get(id);
            if (flowRecord != null) {
                return flowRecord;
            }
        }

        FlowRecord flowRecord = flowRecordRepository.getProcessNodeRecord(id);
        if (flowRecord != null) {
            synchronized (processNodeRecordCache) {
                processNodeRecordCache.put(id, flowRecord);
            }
        }
        return flowRecord;
    }

    /**
     * 批量查询流程记录
     * @param list 流程记录ID
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordByIds(List<Long> list) {
        return flowRecordRepository.findByIds(list);
    }

    /**
     * 查询之前的流程的记录
     * @param processId 流程惟一标识
     * @param recordId 开始记录
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordBeforeRecords(String processId, long recordId) {
        return flowRecordRepository.findBeforeRecords(processId, recordId);
    }

    /**
     * 查询当前流程的记录
     * @param processId 流程唯一标识
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordByProcessId(String processId) {
        return flowRecordRepository.findProcessRecords(processId);
    }

    /**
     * 查询流程节点展示所需的轻量流程记录列表
     *
     * @param processId 流程唯一标识
     * @return 流程记录
     */
    public List<FlowRecord> findProcessNodeRecords(String processId) {
        synchronized (processNodeRecordsCache) {
            List<FlowRecord> flowRecords = processNodeRecordsCache.get(processId);
            if (flowRecords != null) {
                return flowRecords;
            }
        }

        List<FlowRecord> flowRecords = flowRecordRepository.findProcessNodeRecords(processId);
        synchronized (processNodeRecordsCache) {
            processNodeRecordsCache.put(processId, flowRecords);
        }
        return flowRecords;
    }

    private void evictProcessNodeRecords(List<FlowRecord> flowRecords) {
        if (flowRecords == null || flowRecords.isEmpty()) {
            return;
        }
        for (FlowRecord flowRecord : flowRecords) {
            this.evictProcessNodeRecord(flowRecord);
        }
    }

    private void evictProcessNodeRecord(FlowRecord flowRecord) {
        if (flowRecord == null) {
            return;
        }
        synchronized (processNodeRecordCache) {
            processNodeRecordCache.remove(flowRecord.getId());
        }
        synchronized (processNodeRecordsCache) {
            processNodeRecordsCache.remove(flowRecord.getProcessId());
        }
    }

    /**
     * 查询流程之后的记录
     * @param processId 流程唯一标识
     * @param recordId 开始记录
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordAfterRecords(String processId, long recordId) {
        return flowRecordRepository.findAfterRecords(processId,recordId);
    }

    /**
     * 查询流程下的所有待办记录
     * @param processId 流程记录
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordTodoRecords(String processId) {
        return flowRecordRepository.findTodoRecords(processId);
    }

    /**
     * 查询当前节点的流程记录
     * @param fromId 上级id
     * @param nodeId 当前节点id
     * @return 流程记录
     */
    public List<FlowRecord> findFlowRecordCurrentNodeRecords(long fromId, String nodeId) {
        return this.flowRecordRepository.findCurrentNodeRecords(fromId,nodeId);
    }

}
