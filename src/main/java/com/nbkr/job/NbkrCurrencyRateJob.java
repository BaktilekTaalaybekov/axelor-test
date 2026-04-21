package com.nbkr.job;

import com.axelor.inject.Beans;
import com.nbkr.service.NbkrCurrencyRateService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class NbkrCurrencyRateJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      Beans.get(NbkrCurrencyRateService.class).importDailyRates();
    } catch (Exception e) {
      throw new JobExecutionException("Failed to import NBKR daily rates", e);
    }
  }
}
