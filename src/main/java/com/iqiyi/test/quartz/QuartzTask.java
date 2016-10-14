package com.iqiyi.test.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.iqiyi.test.mysql.DBImpl;

public class QuartzTask {
	
	public static void main(String[] args) {  
	     
		//通过schedulerFactory获取一个调度器  
		SchedulerFactory schedulerfactory=new StdSchedulerFactory();  
		Scheduler scheduler=null;  
		try{  
			//通过schedulerFactory获取一个调度器  
			scheduler=schedulerfactory.getScheduler();  
	             
			//创建jobDetail实例，绑定Job实现类  
			//指明job的名称，所在组的名称，以及绑定job类  
			JobDetail job=JobBuilder.newJob(JobDetect.class).withIdentity("job1", "jgroup1").build();  
	             
			//定义调度触发规则  
	             
			//使用simpleTrigger规则    
			//使用cornTrigger规则  每天10点1分  和18点1分  "0 0/10 * * * ? *"   ("0 1 6,12,18,23 * * ? *")) 
			/*Trigger trigger=TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")  
					.withSchedule(CronScheduleBuilder.cronSchedule("0 20 * * * ? *"))  
					.startNow().build();  */ 
			
			String expression = DBImpl.getExpression("1");
			System.out.println("定时任务触发表达式为："+expression);
			Trigger trigger=TriggerBuilder.newTrigger().withIdentity("simpleTrigger", "triggerGroup")  
					.withSchedule(CronScheduleBuilder.cronSchedule(expression))  
					.startNow().build(); 
	             
			//把作业和触发器注册到任务调度中  
			scheduler.scheduleJob(job, trigger);  
	             
			//启动调度  
			scheduler.start();    
       }catch(Exception e){  
           e.printStackTrace();  
       } 	         
	}  
}
