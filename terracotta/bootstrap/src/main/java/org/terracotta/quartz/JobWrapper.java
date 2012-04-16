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

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.impl.JobDetailImpl;

import java.io.Serializable;
import java.util.Map;

class JobWrapper implements Serializable {
  private final JobKey    key;
  private final JobDetail jobDetail;

  JobWrapper(JobDetail jobDetail) {
    this.jobDetail = jobDetail;
    this.key = jobDetail.getKey();
  }

  JobKey getKey() {
    return this.key;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JobWrapper) {
      JobWrapper jw = (JobWrapper) obj;
      if (jw.key.equals(this.key)) { return true; }
    }

    return false;
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return "[" + jobDetail.toString() + "]";
  }

  public boolean requestsRecovery() {
    return jobDetail.requestsRecovery();
  }

  public boolean isConcurrentExectionDisallowed() {
    return jobDetail.isConcurrentExectionDisallowed();
  }

  public boolean isDurable() {
    return jobDetail.isDurable();
  }

  public JobDetail getJobDetailClone() {
    return (JobDetail) jobDetail.clone();
  }

  public void setJobDataMap(JobDataMap newData, Map<JobKey, JobWrapper> map) {
    ((JobDetailImpl) jobDetail).setJobDataMap(newData);
    map.put(key, this);
  }

  public JobDataMap getJobDataMapClone() {
    return (JobDataMap) jobDetail.getJobDataMap().clone();
  }
}