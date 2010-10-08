package org.quartz;

import java.util.Date;
import java.util.Random;

import org.quartz.utils.Key;

public class TriggerBuilder {

    private TriggerKey key;
    private String description;
    private Date startTime = new Date();
    private Date endTime;
    private int priority;
    private String calendarName;
    private JobKey jobKey;
    private JobDataMap jobDataMap = new JobDataMap();
    
    private ScheduleBuilder scheduleBuilder = null;
    
    private TriggerBuilder() {
        
    }
    
    public static TriggerBuilder newTrigger() {
        return new TriggerBuilder();
    }
    
    public Trigger build() {

        if(scheduleBuilder == null)
            scheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        MutableTrigger trig = scheduleBuilder.build();
        
        trig.setCalendarName(calendarName);
        trig.setDescription(description);
        trig.setEndTime(endTime);
        if(key == null)
            key = new TriggerKey(Key.createUniqueName(null), null);
        trig.setKey(key); 
        if(jobKey != null)
            trig.setJobKey(jobKey);
        trig.setPriority(priority);
        trig.setStartTime(startTime);
        
        if(!jobDataMap.isEmpty())
            trig.setJobDataMap(jobDataMap);
        
        return trig;
    }
    
    public TriggerBuilder withIdentity(String name) {
        key = new TriggerKey(name, null);
        return this;
    }  
    
    public TriggerBuilder withIdentity(String name, String group) {
        key = new TriggerKey(name, group);
        return this;
    }
    
    public TriggerBuilder withIdentity(TriggerKey key) {
        this.key = key;
        return this;
    }
    
    public TriggerBuilder withDescription(String description) {
        this.description = description;
        return this;
    }
    
    public TriggerBuilder withPriority(int priority) {
        this.priority = priority;
        return this;
    }
    
    public TriggerBuilder modifiedByCalendar(String calendarName) {
        this.calendarName = calendarName;
        return this;
    }
    
    public TriggerBuilder withStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }
    public TriggerBuilder withStartTimeNow() {
        this.startTime = new Date();
        return this;
    }
    
    public TriggerBuilder withEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }
    
    public TriggerBuilder withSchedule(ScheduleBuilder scheduleBuilder) {
        this.scheduleBuilder = scheduleBuilder;
        return this;
    }
    
    public TriggerBuilder forJob(JobKey jobKey) {
        this.jobKey = jobKey;
        return this;
    }
    
    public TriggerBuilder forJob(String jobName) {
        this.jobKey = new JobKey(jobName, null);
        return this;
    }
    
    public TriggerBuilder forJob(String jobName, String jobGroup) {
        this.jobKey = new JobKey(jobName, jobGroup);
        return this;
    }
    
    public TriggerBuilder forJob(JobDetail jobDetail) {
        JobKey k = jobDetail.getKey();
        if(k.getName() == null)
            throw new IllegalArgumentException("The given job has not yet had a name assigned to it.");
        this.jobKey = k;
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, String value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, Integer value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, Long value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, Float value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, Double value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(String key, Boolean value) {
        jobDataMap.put(key, value);
        return this;
    }
    
    public TriggerBuilder usingJobData(JobDataMap newJobDataMap) {
        // add any existing data to this new map
        for(Object key: jobDataMap.keySet()) {
            newJobDataMap.put(key, jobDataMap.get(key));
        }
        jobDataMap = newJobDataMap; // set new map as the map to use
        return this;
    }
    
}
