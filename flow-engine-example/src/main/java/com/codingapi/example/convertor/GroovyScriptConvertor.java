package com.codingapi.example.convertor;

import com.alibaba.fastjson.JSON;
import com.codingapi.example.entity.GroovyScriptEntity;
import com.codingapi.springboot.script.GroovyScript;
import lombok.SneakyThrows;

import java.util.Map;

public class GroovyScriptConvertor {

    public static GroovyScriptEntity convert(GroovyScript groovyScript) {
        if(groovyScript==null){
            return null;
        }
        GroovyScriptEntity entity = new GroovyScriptEntity();
        entity.setKey(groovyScript.getKey());
        entity.setScript(groovyScript.getScript());
        entity.setDescription(groovyScript.getDescription());
        entity.setMethod(groovyScript.getMethod());
        entity.setReturnType(groovyScript.getReturnType()!=null?groovyScript.getReturnType().getName():null);
        entity.setBinds(groovyScript.getBinds()!=null? JSON.toJSONString(groovyScript.getBinds()):null);
        entity.setRequests(groovyScript.getRequests()!=null? JSON.toJSONString(groovyScript.getRequests()):null);
        entity.setTypeOne(groovyScript.getTypeOne());
        entity.setTypeTwo(groovyScript.getTypeTwo());
        entity.setRemark(groovyScript.getRemark());
        entity.setCreateTime(groovyScript.getCreateTime());
        entity.setUpdateTime(groovyScript.getUpdateTime());
        return entity;
    }

    @SneakyThrows
    public static GroovyScript convert(GroovyScriptEntity entity){
        if(entity==null){
            return null;
        }
        return new GroovyScript(entity.getKey(),
                entity.getScript(),
                entity.getDescription(),
                entity.getMethod(),
                entity.getReturnType()!=null?Class.forName(entity.getReturnType()):null,
                entity.getBinds()!=null?JSON.parseObject(entity.getBinds(), Map.class):null,
                entity.getRequests()!=null?JSON.parseObject(entity.getRequests(), Map.class):null,
                entity.getTypeOne(),
                entity.getTypeTwo(),
                entity.getRemark(),
                entity.getCreateTime(),
                entity.getUpdateTime());
    }
}
