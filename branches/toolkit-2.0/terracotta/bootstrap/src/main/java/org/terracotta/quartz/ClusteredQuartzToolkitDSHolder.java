/* 
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not 
 * use this file except in compliance with the License. You may obtain a copy 
 * of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
 * License for the specific language governing permissions and limitations 
 * under the License.
 * 
 */

package org.terracotta.quartz;

import org.quartz.Calendar;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.terracotta.toolkit.Toolkit;
import org.terracotta.toolkit.collections.ToolkitMap;
import org.terracotta.toolkit.concurrent.locks.ToolkitLock;
import org.terracotta.toolkit.concurrent.locks.ToolkitLockType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * How JOBS mappings will look? <br>
 * JobKey(name, groupname) -> JobWrapper <br>
 * groupName -> List<String> <br>
 * List -> allGroupNames<br>
 */
public class ClusteredQuartzToolkitDSHolder {
  private static final String                                                     JOBS_MAP_PREFIX                     = "_tc_quartz_jobs";
  private static final String                                                     ALL_JOBS_GROUP_NAMES_SET_PREFIX     = "_tc_quartz_grp_names";
  private static final String                                                     PAUSED_GROUPS_SET_PREFIX            = "_tc_quartz_grp_paused_names";
  private static final String                                                     BLOCKED_JOBS_SET_PREFIX             = "_tc_quartz_blocked_jobs";
  private static final String                                                     JOBS_GROUP_MAP_PREFIX               = "_tc_quartz_grp_jobs_";

  private static final String                                                     TRIGGERS_MAP_PREFIX                 = "_tc_quartz_triggers";
  private static final String                                                     TRIGGERS_GROUP_MAP_PREFIX           = "_tc_quartz_grp_triggers_";
  private static final String                                                     ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX = "_tc_quartz_grp_names_triggers";
  private static final String                                                     PAUSED_TRIGGER_GROUPS_SET_PREFIX    = "_tc_quartz_grp_paused_trogger_names";
  private static final String                                                     TIME_TRIGGER_SORTED_SET_PREFIX      = "_tc_time_trigger_sorted_set";
  private static final String                                                     FIRED_TRIGGER_MAP_PREFIX            = "_tc_quartz_fired_trigger";
  private static final String                                                     CALENDAR_WRAPPER_MAP_PREFIX         = "_tc_quartz_calendar_wrapper";
  private static final String                                                     SINGLE_LOCK_NAME_PREFIX             = "_tc_quartz_single_lock";

  private static final String                                                     DELIMETER                           = "|";
  private final String                                                            jobStoreName;
  private final Toolkit                                                           toolkit;
  private final Serializer                                                        serializer;

  private final AtomicReference<SerializedToolkitMap<JobKey, JobWrapper>>         jobsMapReference                    = new AtomicReference<SerializedToolkitMap<JobKey, JobWrapper>>();
  private final AtomicReference<SerializedToolkitMap<TriggerKey, TriggerWrapper>> triggersMapReference                = new AtomicReference<SerializedToolkitMap<TriggerKey, TriggerWrapper>>();

  private final AtomicReference<ClusteredToolkitSet<String>>                      allGroupsReference                  = new AtomicReference<ClusteredToolkitSet<String>>();
  private final AtomicReference<ClusteredToolkitSet<String>>                      allTriggersGroupsReference          = new AtomicReference<ClusteredToolkitSet<String>>();
  private final AtomicReference<ClusteredToolkitSet<String>>                      pausedGroupsReference               = new AtomicReference<ClusteredToolkitSet<String>>();
  private final AtomicReference<ClusteredToolkitSet<JobKey>>                      blockedJobsReference                = new AtomicReference<ClusteredToolkitSet<JobKey>>();
  private final ConcurrentHashMap<String, ClusteredToolkitSet<String>>            jobsGroupSet                        = new ConcurrentHashMap<String, ClusteredToolkitSet<String>>();
  private final ConcurrentHashMap<String, ClusteredToolkitSet<String>>            triggersGroupSet                    = new ConcurrentHashMap<String, ClusteredToolkitSet<String>>();
  private final AtomicReference<ClusteredToolkitSet<String>>                      pausedTriggerGroupsReference        = new AtomicReference<ClusteredToolkitSet<String>>();

  private final AtomicReference<ToolkitMap<String, FiredTrigger>>                 firedTriggersMapReference           = new AtomicReference<ToolkitMap<String, FiredTrigger>>();
  private final AtomicReference<ToolkitMap<String, Calendar>>                     calendarWrapperMapReference         = new AtomicReference<ToolkitMap<String, Calendar>>();

  public ClusteredQuartzToolkitDSHolder(String jobStoreName, Toolkit toolkit, Serializer serializer) {
    this.jobStoreName = jobStoreName;
    this.toolkit = toolkit;
    this.serializer = serializer;
  }

  private final String generateName(String prefix) {
    return prefix + DELIMETER + jobStoreName;
  }

  public Map<JobKey, JobWrapper> getOrCreateJobsMap() {
    String jobsMapName = generateName(JOBS_MAP_PREFIX);
    SerializedToolkitMap<JobKey, JobWrapper> temp = new SerializedToolkitMap<JobKey, JobWrapper>(
                                                                                                 toolkit
                                                                                                     .getMap(jobsMapName),
                                                                                                 serializer);
    jobsMapReference.compareAndSet(null, temp);
    return jobsMapReference.get();
  }

  public Map<TriggerKey, TriggerWrapper> getOrCreateTriggersMap() {
    String triggersMapName = generateName(TRIGGERS_MAP_PREFIX);
    SerializedToolkitMap<TriggerKey, TriggerWrapper> temp = new SerializedToolkitMap<TriggerKey, TriggerWrapper>(
                                                                                                                 toolkit
                                                                                                                     .getMap(triggersMapName),
                                                                                                                 serializer);
    triggersMapReference.compareAndSet(null, temp);
    return triggersMapReference.get();
  }

  public Map<String, FiredTrigger> getOrCreateFiredTriggersMap() {
    String firedTriggerMapName = generateName(FIRED_TRIGGER_MAP_PREFIX);
    ToolkitMap<String, FiredTrigger> temp = toolkit.getMap(firedTriggerMapName);
    firedTriggersMapReference.compareAndSet(null, temp);
    return firedTriggersMapReference.get();
  }

  public Map<String, Calendar> getOrCreateCalendarWrapperMap() {
    String calendarWrapperName = generateName(CALENDAR_WRAPPER_MAP_PREFIX);
    ToolkitMap<String, Calendar> temp = toolkit.getMap(calendarWrapperName);
    calendarWrapperMapReference.compareAndSet(null, temp);
    return calendarWrapperMapReference.get();
  }

  public Set<String> getOrCreateAllGroupsSet() {
    String allGrpSetNames = generateName(ALL_JOBS_GROUP_NAMES_SET_PREFIX);
    ClusteredToolkitSet<String> temp = new ClusteredToolkitSet<String>(toolkit.getList(allGrpSetNames));
    allGroupsReference.compareAndSet(null, temp);

    return allGroupsReference.get();
  }

  public Set<JobKey> getOrCreateBlockedJobsSet() {
    String blockedJobsSetName = generateName(BLOCKED_JOBS_SET_PREFIX);
    ClusteredToolkitSet<JobKey> temp = new ClusteredToolkitSet<JobKey>(toolkit.getList(blockedJobsSetName));
    blockedJobsReference.compareAndSet(null, temp);

    return blockedJobsReference.get();
  }

  public Set<String> getOrCreatePausedGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_GROUPS_SET_PREFIX);
    ClusteredToolkitSet<String> temp = new ClusteredToolkitSet<String>(toolkit.getList(pausedGrpsSetName));
    pausedGroupsReference.compareAndSet(null, temp);

    return pausedGroupsReference.get();
  }

  public Set<String> getOrCreatePausedTriggerGroupsSet() {
    String pausedGrpsSetName = generateName(PAUSED_TRIGGER_GROUPS_SET_PREFIX);
    ClusteredToolkitSet<String> temp = new ClusteredToolkitSet<String>(toolkit.getList(pausedGrpsSetName));
    pausedTriggerGroupsReference.compareAndSet(null, temp);

    return pausedTriggerGroupsReference.get();
  }

  public Set<String> getOrCreateJobsGroupMap(String name) {
    ClusteredToolkitSet<String> set = jobsGroupSet.get(name);

    if (set != null) { return set; }

    String nameForMap = generateName(JOBS_GROUP_MAP_PREFIX + name);
    set = new ClusteredToolkitSet<String>(toolkit.getList(nameForMap));
    ClusteredToolkitSet<String> oldSet = jobsGroupSet.putIfAbsent(name, set);

    return oldSet != null ? oldSet : set;
  }

  public void removeJobsGroupMap(String name) {
    ClusteredToolkitSet set = jobsGroupSet.remove(name);
    if (set != null) {
      set.destroy();
    }
  }

  public Set<String> getOrCreateTriggersGroupMap(String name) {
    ClusteredToolkitSet<String> set = triggersGroupSet.get(name);

    if (set != null) { return set; }

    String nameForMap = generateName(TRIGGERS_GROUP_MAP_PREFIX + name);
    set = new ClusteredToolkitSet<String>(toolkit.getList(nameForMap));
    ClusteredToolkitSet<String> oldSet = triggersGroupSet.putIfAbsent(name, set);

    return oldSet != null ? oldSet : set;
  }

  public void removeTriggersGroupMap(String name) {
    ClusteredToolkitSet set = triggersGroupSet.remove(name);
    if (set != null) {
      set.destroy();
    }
  }

  public Set<String> getOrCreateAllTriggersGroupsSet() {
    String allTriggersGrpsName = generateName(ALL_TRIGGERS_GROUP_NAMES_SET_PREFIX);
    ClusteredToolkitSet<String> temp = new ClusteredToolkitSet<String>(toolkit.getList(allTriggersGrpsName));
    allTriggersGroupsReference.compareAndSet(null, temp);

    return allTriggersGroupsReference.get();
  }

  public TimeTriggerSet getOrCreateTimeTriggerSet() {
    String triggerSetName = generateName(TIME_TRIGGER_SORTED_SET_PREFIX);
    return new TimeTriggerSet(toolkit.getSortedSet(triggerSetName));
  }

  public ToolkitLock getLock(ToolkitLockType lockType) {
    String lockName = generateName(SINGLE_LOCK_NAME_PREFIX);
    return toolkit.getLock(lockName, lockType);
  }
}
