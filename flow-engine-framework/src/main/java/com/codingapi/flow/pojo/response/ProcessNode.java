package com.codingapi.flow.pojo.response;

import com.codingapi.flow.node.IDisplayNode;
import com.codingapi.flow.node.IFlowNode;
import com.codingapi.flow.node.nodes.EndNode;
import com.codingapi.flow.node.nodes.StartNode;
import com.codingapi.flow.operator.IFlowOperator;
import com.codingapi.flow.record.FlowRecord;
import com.codingapi.flow.workflow.Workflow;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 流程审批节点
 */
@Data
@NoArgsConstructor
public class ProcessNode {

    public final static int STATE_HISTORY = -1;
    public final static int STATE_CURRENT = 0;
    public final static int STATE_NEXT = 1;
    /**
     * 节点编号：需要自动生成唯一值
     */
    private String seqNo;
    /**
     * 节点名称
     */
    private String nodeId;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 节点类型
     */
    private String nodeType;

    /**
     * 节点动作类型：代表着流程节点的动作，非节点类型；
     * 比如，节点类型为发起节点时，只有两种类型，即待发起、已发起；
     * 比如，节点类型为审批节点时，只有三种类型，即待审批、已通过、已退回、已拒绝、加签、转办、委派，但这里的状态判定是要根据节点审批人以及审批策略进行判定
     * 比如，节点类型为结束节点时，只有两种类型，即在整个流程未结束时，类型为未结束，否则，类型为已结束
     */
    private String nodeActionType;

    /**
     * 节点类型
     */
    private String nodeActionName;

    /**
     * 是否呈现节点
     */
    private boolean display;

    /**
     * 记录状态
     * -1 为历史状态
     * 0 为当前状态
     * 1 为后续状态
     */
    private int state;


    /**
     * 节点审批人
     */
    private List<FlowOperatorBody> operators;


    public boolean isHistory() {
        return this.state == STATE_HISTORY;
    }

    private ProcessNode(IFlowNode flowNode) {
        this.state = STATE_HISTORY;
        this.operators = new ArrayList<>();
        this.nodeType = flowNode.getType();
        this.nodeName = flowNode.getName();
        this.nodeId = flowNode.getId();
    }

    public static ProcessNode createEndNode(Workflow workflow) {
        IFlowNode endNode = workflow.getEndNode();
        ProcessNode node = new ProcessNode(endNode);
        node.nodeActionType = "FINISH";
        node.nodeActionName = "已结束";
        return node;
    }

    public ProcessNode(FlowRecord flowRecord, Workflow workflow) {
        this.nodeId = flowRecord.getNodeId();
        IFlowNode flowNode = workflow.getFlowNode(this.nodeId);
        this.nodeName = flowNode.getName();
        this.nodeType = flowNode.getType();
        this.operators = new ArrayList<>();
        this.display = true;
        this.state = STATE_HISTORY;
        this.nodeActionType = flowRecord.getActionType();
        this.nodeActionName = toHistoryActionName(flowRecord.getActionName());
        this.operators.add(new FlowOperatorBody(flowRecord));
    }


    public ProcessNode(IFlowNode flowNode, List<IFlowOperator> operators) {
        this.nodeId = flowNode.getId();
        this.nodeName = flowNode.getName();
        this.nodeType = flowNode.getType();
        this.operators = operators.stream().map(FlowOperatorBody::new).toList();
        this.state = STATE_NEXT;
        this.display = flowNode instanceof IDisplayNode;
        this.nodeActionType = "PENDING";
        this.nodeActionName = toPendingActionName(flowNode.getType());
    }


    public boolean isFlowNode(IFlowNode currentNode) {
        return this.nodeId.equals(currentNode.getId());
    }

    public void setCurrentState() {
        this.state = STATE_CURRENT;
    }


    @Override
    public boolean equals(Object target) {
        if (target instanceof ProcessNode) {
            ProcessNode targetNode = (ProcessNode) target;
            return targetNode.getNodeId().equals(this.getNodeId());
        }
        return super.equals(target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeId);
    }

    public void addOperator(FlowOperatorBody operator) {
        this.operators.add(operator);
    }

    private static String toHistoryActionName(String actionName) {
        if (actionName == null) {
            return null;
        }
        return switch (actionName) {
            case "通过" -> "已通过";
            case "拒绝" -> "已拒绝";
            case "退回" -> "已退回";
            case "加签" -> "加签";
            case "转办" -> "转办";
            case "委派" -> "委派";
            case "保存" -> "已发起";
            default -> "已" + actionName;
        };
    }

    private static String toPendingActionName(String nodeType) {
        if (StartNode.NODE_TYPE.equals(nodeType)) {
            return "待发起";
        }
        if (EndNode.NODE_TYPE.equals(nodeType)) {
            return "未结束";
        }
        return "待审批";
    }

    /**
     * 审批意见内容，仅当历史节点存在数据
     */
    @Data
    @NoArgsConstructor
    public static class FlowOperatorBody {

        /**
         * 审批意见
         */
        private String advice;

        /**
         * 签名key
         */
        private String signKey;

        /**
         * 审批动作
         */
        private String actionName;
        /**
         * 审批人
         */
        private FlowOperator flowOperator;
        /**
         * 审批时间
         */
        private long approveTime;

        public FlowOperatorBody(FlowRecord flowRecord) {
            this.advice = flowRecord.getAdvice();
            this.signKey = flowRecord.getSignKey();
            this.approveTime = flowRecord.getCreateTime();
            this.actionName = flowRecord.getActionName();
            this.flowOperator = new FlowOperator(flowRecord.getCurrentOperatorId(), flowRecord.getCurrentOperatorName());
        }

        public FlowOperatorBody(IFlowOperator flowOperator) {
            this.flowOperator = new FlowOperator(flowOperator);
        }

    }

}
