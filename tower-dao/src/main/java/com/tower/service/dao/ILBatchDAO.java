package com.tower.service.dao;

import java.util.List;
import java.util.Map;

/**
 * 批处理数据访问层接口
 * 
 * @author alexzhu
 *
 * @param <T>
 */
public interface ILBatchDAO<T> extends IBatchDAO<T>{
  /**
   * 批量插入
   * 
   * @param datas
   * @param tabNameSuffix
   *          表名后缀［用于支持表拆分机制，即：数据库操作时的表名规则为:tableName+"_"+tabNameSuffix］
   * @return
   */
  public Integer batchInsert(List<Map<String,Object>> datas, String tabNameSuffix);
  
}
