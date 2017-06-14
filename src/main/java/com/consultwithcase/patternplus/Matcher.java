//"Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements; and to You under the Apache License, Version 2.0. "
package com.consultwithcase.patternplus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.MatchResult;

/**
 *
 */
public class Matcher implements MatchResult {
    java.util.regex.Matcher m;

    private boolean matched = false;
   
    
    public Matcher(java.util.regex.Matcher matcher) {
        this.m = matcher;
    }
    
    @Override
    public int start() {
        return m.start();
    }

    @Override
    public int start(int group) {
        return m.start(group);
    }

    @Override
    public int end() {
        return m.end();
    }

    @Override
    public int end(int group) {
        return m.end(group);
    }

    @Override
    public String group() {
        return m.group();
    }

    @Override
    public String group(int group) {
        return m.group(group);
    }

    @Override
    public int groupCount() {
        return m.groupCount();
    }
    
    public int end(String name) {
        return m.end(name);
    }
    
    public boolean find() {
        return m.find();
    }
    
    public boolean find(int start){
        return m.find(start);
    }
    
    public String group(String name) {
        return m.group(name);
    }
    
    public boolean hasAnchoringBounds() {
        return m.hasAnchoringBounds();
    }
    
    public boolean hasTransparentBounds() {
        return m.hasTransparentBounds();
    }
    
    public boolean hitEnd() {
        return m.hitEnd();
    }
    
    public boolean lookingAt() {
        return m.lookingAt();
    }
     
    public boolean matches() {
        matched = m.matches();
        return matched;
    }
    
    public int regionEnd() {
        return m.regionEnd();
    }
    
    public int regionStart() {
        return m.regionStart();
    }
    
    public String replaceAll(String replacement) {
        return m.replaceAll(replacement);
    }
    
    public String replaceFirst(String replacement) {
        return m.replaceFirst(replacement);
    }
    
    public boolean requireEnd() {
        return m.requireEnd();
    }
    
    @Override
    public String toString() {
        return m.toString();
    }
    
    public enum InjectOption {
        PUBLIC_FIELD, POJO, METHOD_NAME;
    }
    
    /**
     * takes the pattern and line constructed by this pattern and populates
     * the object o provide via InjectOption provided.
     * @param o -- object to perform the injection on
     * @param option -- option to utilize
     * @return -- returns Set&lt;String&gt; of the group that wasn't injected into the Object o
     */
    public Set<String> inject(Object o, InjectOption option) {
        Set<String> groups = getGroupsStrings();
        Map<String, String> groupMethodPair;
        switch(option) {
            case PUBLIC_FIELD:
                injectByField(o, groups);
                break;
            case POJO:
                groupMethodPair = pojoSetMethod(groups);
                injectByMethod(o, groupMethodPair, groups);
                break;
            case METHOD_NAME:
                injectByMethod(o, groups);
                break;
                    
        }
        return groups;
    }
    
    
    private Method[] generateAllMethods(Object o) {
        return o.getClass().getMethods();
    }
    
    private boolean validMethod(Method method) {
        if(method != null) {
            
                Parameter p[] = method.getParameters();
                return p.length == 1 && p[0].getType().equals(String.class);
            }
        
        return false;
    }
    
    private void injectByMethod(Object o, Set<String> groups) {
        if(this.matches()) {
            for(Method method : generateAllMethods(o)) {
                if(groups.contains(method.getName())) {
                    if(attemptToInject(o, method, method.getName())) {
                        groups.remove(method.getName());
                    }
                }
                    
            }
        }
        
        
    }
    
    private boolean attemptToInject(Object o, Method method, String regexGroup) {
        if(validMethod(method)) {
        try {
            method.invoke(o, this.group(regexGroup));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        }
        return false;
    }
    
    private void injectByMethod(Object o, Map<String,String> methodGroupPair, Set<String> groups) {
        if(this.matches()) {
            for(Method method : generateAllMethods(o)) {
                if(methodGroupPair.containsKey(method.getName())) {
                    String group = methodGroupPair.get(method.getName());
                    if(attemptToInject(o, method, group)) {
                        groups.remove(group);
                    }
                }
            }
        }
    }
    
    private Map<String, String> pojoSetMethod(Set<String> fieldNames) {
        Map<String, String> pojoMethod = new HashMap<>();
        for(String str : fieldNames) {
            pojoMethod.put(
                    String.format("set%s%s",
                    str.substring(0, 1).toUpperCase(),
                    str.substring(1)),
                    str
            );
        }
        return pojoMethod;
    }
    
    private void injectByField(Object o, Set<String> groups) {
        if(this.matches()) {
            Field fields[] = o.getClass().getFields();
            for(Field field : fields) {
                if(groups.contains(field.getName())) {
                    try {
                        field.set(o, this.group(field.getName()));
                        groups.remove(field.getName());
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    
    private static String GROUP_NAME_PATTERN_REGEX = 
            "<(.*?)>";
    /**
     * Finds all the groups name looking in the pattern (?<groupName>)
     * @return 
     */
    private Set<String> getGroupsStrings() {
        String pattern = m.pattern().pattern();
        Pattern p = Pattern.compile(GROUP_NAME_PATTERN_REGEX);
        Matcher mm = p.matcher(pattern);
        Set<String> groups = new TreeSet<>(); 
        while(mm.find()) {
            groups.add(mm.group(1));
        }
        return groups;
    }
    
    
    
    
}
