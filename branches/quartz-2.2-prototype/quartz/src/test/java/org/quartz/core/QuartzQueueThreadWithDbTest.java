/*
 * Copyright 2012 Terracotta, Inc.
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
package org.quartz.core;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.QueueJobDetail;
import org.quartz.QueueJobManager;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.core.QuartzQueueThreadTest.MyQueueJob;
import org.quartz.impl.QueueJobDetailImpl;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Integration/Unit test for QueueJobThread.
 * 
 * @author Zemian Deng
 */
public class QuartzQueueThreadWithDbTest {
	@Test
    public void testStartShutdown() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QuartzQueueThreadTest-mysql-quartz.properties");
        Scheduler scheduler = fac.getScheduler();
        scheduler.start();
        Thread.sleep(3000);
        scheduler.shutdown();
    }
	
    @Test
    public void testQueueJobManager() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QuartzQueueThreadTest-mysql-quartz.properties");
    	Scheduler scheduler = fac.getScheduler();
        
        QueueJobManager queueMgr = scheduler.getQueueJobManager();
        assertThat(queueMgr.getQueueJobKeys().size(), is(0));
        
        QueueJobDetailImpl job = new QueueJobDetailImpl();
        job.setDescription("Test job");
        job.setPriority(5);
        job.setKey(JobKey.jobKey("test"));
        job.setJobClass(MyQueueJob.class);
        queueMgr.addQueueJobDetail(job);
        assertThat(queueMgr.getQueueJobKeys().size(), is(1));
        
        queueMgr.removeQueueJobDetail(JobKey.jobKey("test"));
        assertThat(queueMgr.getQueueJobKeys().size(), is(0));
        
        // Add a batch
        int batchSize = 100;
        for (int i=0; i < batchSize; i++) {
        	job = new QueueJobDetailImpl();
            job.setDescription("Test job" + i);
            job.setPriority(5);
            job.setKey(JobKey.jobKey("test" + i));
            job.setJobClass(MyQueueJob.class);
            queueMgr.addQueueJobDetail(job);
        }
        assertThat(queueMgr.getQueueJobKeys().size(), is(batchSize));
        
        queueMgr.removeQueueJobDetail(JobKey.jobKey("test" + 17));
        assertThat(queueMgr.getQueueJobKeys().size(), is(batchSize - 1));
        
        job = (QueueJobDetailImpl)queueMgr.getQueueJobDetail(JobKey.jobKey("test" + 10));
        job.setPriority(9);
        queueMgr.updateQueueJobDetail(job);
        assertThat(queueMgr.getQueueJobKeys().size(), is(batchSize - 1));
        
        QueueJobDetail job2 = queueMgr.getQueueJobDetail(JobKey.jobKey("test" + 10));
        assertThat(job2.getPriority(), is(9));
        
        // Delete the batch
        for (int i=0; i < batchSize; i++) {
        	if (i == 17)
        		continue;
        	queueMgr.removeQueueJobDetail(JobKey.jobKey("test" + i));
        }
        assertThat(queueMgr.getQueueJobKeys().size(), is(0));
        
        scheduler.shutdown();
    }
    
    @Test
    public void testRunQueueJob() throws Exception {
    	SchedulerFactory fac = new StdSchedulerFactory("org/quartz/core/QuartzQueueThreadTest-mysql-quartz.properties");
    	Scheduler scheduler = fac.getScheduler();
        QueueJobManager queueMgr = scheduler.getQueueJobManager();
        try {
        	if (queueMgr.checkQueueJobExists(JobKey.jobKey("test1")))
        		queueMgr.removeQueueJobDetail(JobKey.jobKey("test1"));
        	if (queueMgr.checkQueueJobExists(JobKey.jobKey("test2")))
        		queueMgr.removeQueueJobDetail(JobKey.jobKey("test2"));       	
        	if (queueMgr.checkQueueJobExists(JobKey.jobKey("test3")))
        		queueMgr.removeQueueJobDetail(JobKey.jobKey("test3"));
        	
	        queueMgr.addQueueJobDetail(createQueueJob("test2", 5));       
	        queueMgr.addQueueJobDetail(createQueueJob("test3", 7));	        
	        queueMgr.addQueueJobDetail(createQueueJob("test1", 1));

	        scheduler.start();
	        Thread.sleep(10000L);
        } finally {
        	scheduler.shutdown();
        }
    }
    
    private QueueJobDetailImpl createQueueJob(String name, int priority) {
    	QueueJobDetailImpl job = new QueueJobDetailImpl();
        job.setDescription("Test job");
        job.setPriority(priority);
        job.setKey(JobKey.jobKey(name));
        job.setJobClass(MyQueueJob.class);
        return job;
    }
    
    public static class MyQueueJob implements Job {

		public void execute(JobExecutionContext context) throws JobExecutionException {
			System.out.println("Executing job: " + context);
		}
    	
    }
}
