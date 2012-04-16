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
import org.quartz.spi.OperableTrigger;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

class TriggerWrapper implements Serializable {
  enum TriggerState {
    WAITING, ACQUIRED, COMPLETE, PAUSED, BLOCKED, PAUSED_BLOCKED, ERROR;
  }

  private final TriggerKey      key;
  private final JobKey          jobKey;
  private final boolean         jobDisallowsConcurrence;

  private volatile String       lastTerracotaClientId = null;
  private volatile TriggerState state                 = TriggerState.WAITING;

  private final OperableTrigger trigger;

  TriggerWrapper(OperableTrigger trigger, boolean jobDisallowsConcurrence, Serializer serializer) {
    this.key = trigger.getKey();
    this.jobKey = trigger.getJobKey();
    this.trigger = trigger;
    this.jobDisallowsConcurrence = jobDisallowsConcurrence;

    // TriggerWrapper instances get shared in many collections and the serialized form
    // might be referenced before this wrapper makes it into the "timeTriggers" set
    // DEV-4807
    // serialize(serializer);
  }

  boolean jobDisallowsConcurrence() {
    return jobDisallowsConcurrence;
  }

  String getLastTerracotaClientId() {
    return lastTerracotaClientId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TriggerWrapper) {
      TriggerWrapper tw = (TriggerWrapper) obj;
      if (tw.getKey().equals(this.getKey())) { return true; }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return getKey().hashCode();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "( " + state + ", lastTC=" + lastTerracotaClientId + ", " + key + ", "
           + trigger + ")";
  }

  public TriggerKey getKey() {
    return key;
  }

  JobKey getJobKey() {
    return jobKey;
  }

  void setState(TriggerState state, String terracottaId, Map<TriggerKey, TriggerWrapper> map) {
    if (terracottaId == null) { throw new NullPointerException(); }

    this.state = state;
    this.lastTerracotaClientId = terracottaId;

    rePut(map);
  }

  TriggerState getState() {
    return state;
  }

  public Date getNextFireTime() {
    return this.trigger.getNextFireTime();
  }

  public int getPriority() {
    return this.trigger.getPriority();
  }

  public boolean mayFireAgain() {
    return this.trigger.mayFireAgain();
  }

  public OperableTrigger getTriggerClone() {
    return (OperableTrigger) this.trigger.clone();
  }

  public void updateWithNewCalendar(Calendar cal, long misfireThreshold, Map<TriggerKey, TriggerWrapper> map) {
    this.trigger.updateWithNewCalendar(cal, misfireThreshold);
    rePut(map);
  }

  public String getCalendarName() {
    return this.trigger.getCalendarName();
  }

  public int getMisfireInstruction() {
    return this.trigger.getMisfireInstruction();
  }

  public void updateAfterMisfire(Calendar cal, Map<TriggerKey, TriggerWrapper> map) {
    this.trigger.updateAfterMisfire(cal);
    rePut(map);
  }

  public void setFireInstanceId(String firedInstanceId, Map<TriggerKey, TriggerWrapper> map) {
    this.trigger.setFireInstanceId(firedInstanceId);
    rePut(map);
  }

  public void triggered(Calendar cal, Map<TriggerKey, TriggerWrapper> map) {
    this.trigger.triggered(cal);
    rePut(map);
  }

  private void rePut(Map<TriggerKey, TriggerWrapper> map) {
    map.put(key, this);
  }
}