package fit.iuh.student.schedulingservice.config;

import fit.iuh.student.schedulingservice.jobs.AppointmentReminderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail appointmentReminderJobDetail() {
        return JobBuilder.newJob(AppointmentReminderJob.class)
                .withIdentity("appointmentReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger appointmentReminderJobTrigger() {
        // Run every 30 minutes
        SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder
                .simpleSchedule()
                .withIntervalInMinutes(30)
                .repeatForever();

        return TriggerBuilder.newTrigger()
                .forJob(appointmentReminderJobDetail())
                .withIdentity("appointmentReminderTrigger")
                .withSchedule(scheduleBuilder)
                .build();
    }
}