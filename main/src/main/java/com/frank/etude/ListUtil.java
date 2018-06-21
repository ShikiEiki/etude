package com.frank.etude;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FH on 2017/4/5.
 */


public class ListUtil {
    /**
     * List.remove()的条件扩展模式,可以自定义规则,移除List中符合规则的节点.
     * <p>
     * 给定一个列表,和一套比对规则,根据规则逐一的比对列表中的数据,去除列表里所有符合条件的节点,并且返回去除的节点数.
     * <p>比如给定要去除mList中所有id为"test"的节点,则可以如此调用
     *
     * <p>  conditionalRemove(mList , new ConditionJudger<Node>() {
     * <p>      @Override
     * <p>      public boolean isMatchCondition(Node nodeInList) {
     * <p>          return "test".equals(nodeInList.id);
     * <p>      }
     * <p>  });
     *
     * @param targetList 需要去除的列表
     * @param judger 比对规则判断器
     * @return 去除的节点数
     */
    public static <T> int conditionalRemove(List<T> targetList , ConditionJudger<T> judger){
        int totalRemove = 0;
        if (judger != null){
            for (int i = 0 ; i < targetList.size() ;){
                T nodeInList = targetList.get(i);
                if (judger.isMatchCondition(nodeInList)){
                    targetList.remove(nodeInList);
                    totalRemove++;
                }
                else {
                    i++;
                }
            }
        }
        return totalRemove;
    }

    /**
     * List.subList()的条件扩展,可以自定义规则,返回一个包含所有List中符合条件节点的新的List.
     * <p>
     * 给定一个列表和一套比对规则,根据规则,逐一比对列表中的每一个节点是否符合规则,把所有符合规则的节点组成一个新的ArrayList返回
     * <p>
     * 比如要得到mList中所有content为"test"的节点组成的子列表,可以如此调用:
     *
     * <p>  List subList = conditionalSubList(mList, new ConditionJudger<Node>() {
     * <p>      @Override
     * <p>      public boolean isMatchCondition(Node nodeInList) {
     * <p>          return nodeInList.content.equals("test");
     * <p>      }
     * <p>  });
     *
     * @param targetList 给定的列表
     * @param judger 给定的规则
     * @return 符合规则的新列表
     */
    public static <T> ArrayList conditionalSubList(List<T> targetList , ConditionJudger<T> judger){
        ArrayList<T> subList = new ArrayList();
        if (judger != null){
            for (T nodeInList : targetList) {
                if (judger.isMatchCondition(nodeInList)) {
                    subList.add(nodeInList);
                }
            }
        }
        return subList;
    }

    /**
     * List.contains()的条件扩展,可以自定义规则判断List中是否有符合条件的节点,以boolean的形式返回.
     * <p>
     * 给定一个列表,和一套比对规则,根据规则,比对列表中的每一项是否符合规则.
     * 如果至少存在一个节点符合规则,则返回true,否则,任何一个节点都不符合规则,返回false.
     * 例如判断mList中是否有和accordingNode一样id的节点存在,则可以如此调用:
     *
     * <p>  Node accordingNode = ....;
     * <p>  conditionalContains(mList , new ConditionJudger<Node>() {
     * <p>      @Override
     * <p>      public boolean isMatchCondition(Node nodeInList) {
     * <p>          return nodeInList.id.equals(accordingObj.id);
     * <p>      }
     * <p>  });
     *
     * @param targetList 给定的列表
     * @param judger 给定的规则判断器
     *
     * @return 如果至少存在一个节点符合规则,则返回true,否则,任何一个节点都不符合规则,返回false.
     */
    public static <T> boolean conditionalContains(List<T> targetList , ConditionJudger<T> judger){
        for (T nodeInList : targetList) {
            if (judger.isMatchCondition(nodeInList)){
                return true;
            }
        }
        return false;
    }


    public interface ConditionJudger<T>{
        public boolean isMatchCondition(T nodeInList);
    }
}
