package com.codingapi.example.repository.impl;

import com.codingapi.example.convertor.GroovyScriptConvertor;
import com.codingapi.example.entity.GroovyScriptEntity;
import com.codingapi.example.repository.GroovyScriptEntityRepository;
import com.codingapi.springboot.script.GroovyScript;
import com.codingapi.springboot.script.repository.GroovyScriptRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class GroovyScriptRepositoryImpl implements GroovyScriptRepository {

    private final GroovyScriptEntityRepository groovyScriptEntityRepository;

    @Override
    public void save(GroovyScript groovyScript) {
        GroovyScriptEntity entity = GroovyScriptConvertor.convert(groovyScript);
        if(entity!=null){
            groovyScriptEntityRepository.save(entity);
        }
    }

    @Override
    public void delete(GroovyScript groovyScript) {
        if(groovyScript!=null){
            groovyScriptEntityRepository.deleteById(groovyScript.getKey());
        }
    }
}
