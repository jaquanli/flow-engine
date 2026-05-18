package com.codingapi.flow.service.impl;

import com.codingapi.flow.action.IFlowAction;
import com.codingapi.flow.action.actions.PassAction;
import com.codingapi.flow.exception.FlowNotFoundException;
import com.codingapi.flow.form.FormData;
import com.codingapi.flow.manager.ActionManager;
import com.codingapi.flow.manager.OperatorManager;
import com.codingapi.flow.node.IDisplayNode;
import com.codingapi.flow.node.IFlowNode;
import com.codingapi.flow.node.nodes.EndNode;
import com.codingapi.flow.node.nodes.StartNode;
import com.codingapi.flow.operator.IFlowOperator;
import com.codingapi.flow.pojo.request.FlowProcessNodeRequest;
import com.codingapi.flow.pojo.response.ProcessNode;
import com.codingapi.flow.record.FlowRecord;
import com.codingapi.flow.service.FlowRecordService;
import com.codingapi.flow.service.WorkflowService;
import com.codingapi.flow.session.FlowAdvice;
import com.codingapi.flow.session.FlowSession;
import com.codingapi.flow.session.IRepositoryHolder;
import com.codingapi.flow.strategy.node.OperatorLoadStrategy;
import com.codingapi.flow.strategy.node.OperatorSelectType;
import com.codingapi.flow.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

/**
 * 流程节点记录服务
 */
@Slf4j
public class FlowProcessNodeService {

    private final FlowProcessNodeRequest request;
    private final FlowRecordService flowRecordService;
    private final WorkflowService workflowService;
    private final IRepositoryHolder repositoryHolder;

    // 当前操作人
    private final IFlowOperator currentOperator;
    // 当前的流程记录，当id为workId时flowRecord为空
    private FlowRecord flowRecord;
    // 当前的流程设计器
    private Workflow workflow;
    // 流程节点记录
    private final List<ProcessNode> nodeList;
    // 流程审批记录列表
    private final Map<Long, IFlowOperator> recordOperatorMap;
    // 当前流程实例下的全部流程记录
    private List<FlowRecord> processRecords;


    public FlowProcessNodeService(FlowProcessNodeRequest request, IRepositoryHolder repositoryHolder) {
        this.request = request;
        this.flowRecordService = repositoryHolder.getFlowRecordService();
        this.workflowService = repositoryHolder.getWorkflowService();
        this.repositoryHolder = repositoryHolder;
        this.nodeList = new ArrayList<>();
        this.recordOperatorMap = new HashMap<>();
        this.currentOperator = this.loadRecordOperator(request.getOperatorId());
        this.initData();
    }


    private void initData() {
        String id = this.request.getId();
        if (this.isCreateWorkflow()) {
            this.workflow = workflowService.getWorkflow(id);
        } else {
            this.flowRecord = flowRecordService.getProcessNodeRecord(Long.parseLong(id));
            if (flowRecord == null) {
                throw FlowNotFoundException.record(Long.parseLong(id));
            }
            this.workflow = workflowService.getRuntimeWorkflow(flowRecord.getWorkRuntimeId());
        }
    }

    private boolean isCreateWorkflow() {
        String id = this.request.getId();
        return !id.matches("^[0-9]+$");
    }

    private IFlowOperator loadRecordOperator(long operatorId) {
        IFlowOperator flowOperator = this.recordOperatorMap.get(operatorId);
        if (flowOperator != null) {
            return flowOperator;
        }

        flowOperator = this.repositoryHolder.getOperatorById(operatorId);
        if (flowOperator != null) {
            this.recordOperatorMap.put(flowOperator.getUserId(), flowOperator);
        }
        return flowOperator;
    }

    private void preloadRecordOperators(Collection<Long> operatorIds) {
        if (operatorIds == null || operatorIds.isEmpty()) {
            return;
        }

        List<Long> ids = operatorIds.stream()
                .filter(id -> id != null && id > 0)
                .filter(id -> !this.recordOperatorMap.containsKey(id))
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return;
        }

        List<IFlowOperator> operators = this.repositoryHolder.findOperatorByIds(ids);
        if (operators == null || operators.isEmpty()) {
            return;
        }

        for (IFlowOperator operator : operators) {
            if (operator != null) {
                this.recordOperatorMap.put(operator.getUserId(), operator);
            }
        }
    }

    private List<Long> collectRecordOperatorIds(Collection<FlowRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }

        List<Long> ids = new ArrayList<>();
        for (FlowRecord record : records) {
            ids.add(record.getCurrentOperatorId());
            ids.add(record.getCreateOperatorId());
            ids.add(record.getSubmitOperatorId());
            ids.add(record.getForwardOperatorId());
            ids.add(record.getInterferedOperatorId());
        }
        return ids;
    }


    public List<ProcessNode> processNodes() {
        long start = System.currentTimeMillis();
        // load history data
        if (this.flowRecord != null) {
            long historyStart = System.currentTimeMillis();
            this.loadHistoryData();
            log.debug("flow process nodes load history data cost: {}ms", System.currentTimeMillis() - historyStart);
            if (this.flowRecord.isFinish()) {
                // load end node
                this.loadEndNode(this.flowRecord.isFinish());
                log.debug("flow process nodes total cost: {}ms", System.currentTimeMillis() - start);
                return nodeList;
            }
        }
        // load next node data
        long nextStart = System.currentTimeMillis();
        this.loadNextData();
        log.debug("flow process nodes load next data cost: {}ms", System.currentTimeMillis() - nextStart);
        // load end node
        this.loadEndNode(false);
        log.debug("flow process nodes total cost: {}ms", System.currentTimeMillis() - start);
        return nodeList;
    }


    private void loadHistoryData() {
        long queryStart = System.currentTimeMillis();
        List<FlowRecord> allRecords = flowRecordService.findProcessNodeRecords(this.flowRecord.getProcessId());
        log.debug("flow process nodes query history records cost: {}ms, size: {}", System.currentTimeMillis() - queryStart, allRecords.size());
        this.processRecords = allRecords;
        // 预加载操作人
        long operatorStart = System.currentTimeMillis();
        List<Long> operatorIds = new ArrayList<>(this.collectRecordOperatorIds(allRecords));
        operatorIds.add(this.request.getOperatorId());
        this.preloadRecordOperators(operatorIds);
        log.debug("flow process nodes preload record operators cost: {}ms", System.currentTimeMillis() - operatorStart);
        long orderStart = System.currentTimeMillis();
        FlowRecordOrderService orderService = new FlowRecordOrderService(allRecords, this::loadRecordOperator, flowRecords -> nodeList.add(ProcessNode.createByRecord(flowRecords, workflow)));
        orderService.fetch(0);
        log.debug("flow process nodes build history nodes cost: {}ms", System.currentTimeMillis() - orderStart);
    }

    private void loadEndNode(boolean finish) {
        IFlowNode endNode = this.workflow.getEndNode();
        this.nodeList.add(ProcessNode.createByEndNode(endNode, finish));
    }


    private void loadNextData() {
        if (this.flowRecord == null) {
            IFlowNode currentNode = this.workflow.getStartNode();
            FlowSession flowSession = this.buildFlowSession(currentNode, currentOperator, currentOperator, currentOperator, 0);
            this.addFlowNode(currentNode, flowSession);
            long fetchStart = System.currentTimeMillis();
            this.fetchFlowNode(flowSession);
            log.debug("flow process nodes fetch start next nodes cost: {}ms", System.currentTimeMillis() - fetchStart);
        } else {
            List<FlowRecord> todoRecords = this.loadTodoRecords();
            if (todoRecords != null && !todoRecords.isEmpty()) {
                long operatorStart = System.currentTimeMillis();
                this.preloadRecordOperators(this.collectRecordOperatorIds(todoRecords));
                log.debug("flow process nodes preload todo operators cost: {}ms", System.currentTimeMillis() - operatorStart);
                for (FlowRecord todoRecord : todoRecords) {
                    IFlowNode currentNode = this.workflow.getFlowNode(todoRecord.getNodeId());
                    IFlowOperator createOperator = this.loadRecordOperator(todoRecord.getCreateOperatorId());
                    IFlowOperator submitOperator = this.loadRecordOperator(todoRecord.getSubmitOperatorId());

                    FlowSession flowSession = this.buildFlowSession(currentNode, currentOperator, createOperator, submitOperator, todoRecord.getWorkRuntimeId());
                    long fetchStart = System.currentTimeMillis();
                    this.fetchFlowNode(flowSession);
                    log.debug("flow process nodes fetch next nodes cost: {}ms, recordId: {}", System.currentTimeMillis() - fetchStart, todoRecord.getId());
                }
            }
        }
    }

    private List<FlowRecord> loadTodoRecords() {
        if (this.processRecords != null) {
            return this.processRecords.stream()
                    .filter(FlowRecord::isTodo)
                    .toList();
        }
        return this.flowRecordService.findFlowRecordTodoRecords(flowRecord.getProcessId());
    }


    private FlowSession buildFlowSession(IFlowNode currentNode,
                                         IFlowOperator currentOperator,
                                         IFlowOperator createdOperator,
                                         IFlowOperator submitOperator,
                                         long backupId) {
        ActionManager actionManager = currentNode.actionManager();
        IFlowAction flowAction = actionManager.getAction(PassAction.class);
        FormData formData = new FormData(this.workflow.getForm());
        formData.reset(this.request.getFormData());

        return new FlowSession(
                this.repositoryHolder,
                currentOperator,
                createdOperator,
                submitOperator,
                this.workflow,
                currentNode,
                flowAction,
                formData,
                this.flowRecord,
                new ArrayList<>(),
                backupId,
                FlowAdvice.nullFlowAdvice()
        );
    }


    private void fetchFlowNode(FlowSession flowSession) {
        List<IFlowNode> nextNodes = flowSession.matchNextNodes();
        if (nextNodes != null && !nextNodes.isEmpty()) {
            for (IFlowNode flowNode : nextNodes) {
                this.addFlowNode(flowNode, flowSession);

                FlowSession nextSession = flowSession.updateSession(flowNode);
                this.fetchFlowNode(nextSession);
            }
        }

    }

    private void addFlowNode(IFlowNode flowNode, FlowSession flowSession) {
        // 仅添加展示节点，且非结束节点
        if (flowNode instanceof IDisplayNode) {
            if (!(flowNode instanceof EndNode)) {

                if (flowNode instanceof StartNode) {
                    List<IFlowOperator> operators = new ArrayList<>();
                    operators.add(this.currentOperator);
                    this.nodeList.add(ProcessNode.createByNode(flowNode, OperatorSelectType.SCRIPT, operators));
                } else {
                    OperatorManager operatorManager = flowNode.strategyManager().loadOperators(flowSession);
                    List<IFlowOperator> operators = operatorManager.getOperators();

                    OperatorSelectType operatorSelectType = null;
                    // 针对延迟节点、触发节点、子流程节点、路由节点、人工节点都没有设置流程审批人
                    OperatorLoadStrategy operatorLoadStrategy = flowNode.strategyManager().getStrategy(OperatorLoadStrategy.class);
                    if(operatorLoadStrategy!=null) {
                        operatorSelectType = operatorLoadStrategy.getSelectType();
                    }

                    this.nodeList.add(ProcessNode.createByNode(flowNode, operatorSelectType, operators));
                }
            }
        }
    }


    private interface IFlowOperatorGateway {

        IFlowOperator getFlowOperator(long operatorId);
    }

    private static class FlowRecordOrderService {

        private final List<FlowRecord> flowRecords;

        private final Consumer<List<ProcessNode.FlowRecordOperator>> consumer;

        private final IFlowOperatorGateway flowOperatorGateway;


        public FlowRecordOrderService(List<FlowRecord> flowRecords, IFlowOperatorGateway flowOperatorGateway, Consumer<List<ProcessNode.FlowRecordOperator>> consumer) {
            this.consumer = consumer;
            this.flowOperatorGateway = flowOperatorGateway;
            this.flowRecords = flowRecords.stream().sorted(Comparator.comparing(FlowRecord::getId)).toList();
        }


        private List<FlowRecord> getNextRecords(long formId) {
            List<FlowRecord> recordList = new ArrayList<>();
            for (FlowRecord record : this.flowRecords) {
                if (record.getFromId() == formId) {
                    recordList.add(record);
                }
            }
            return recordList;
        }


        public void fetch(long formId) {
            List<FlowRecord> batchList = this.getNextRecords(formId);
            if (!batchList.isEmpty()) {
                this.consumer.accept(batchList.stream().map(record -> new ProcessNode.FlowRecordOperator(record, flowOperatorGateway.getFlowOperator(record.getCurrentOperatorId()))).toList());

                for (FlowRecord item : batchList) {
                    this.fetch(item.getId());
                }
            }
        }

    }

}
