package com.siyue.job;

import com.actionsoft.bpms.schedule.IJob;
import com.siyue.service.EscapeeService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by Administrator on 2018/12/27.
 */
public class EscapeeJob implements IJob {

    private EscapeeService escapeeService = new EscapeeService();

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        escapeeService.addEscapee();
    }
}
