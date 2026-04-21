package com.nbkr.db.repo;

import com.axelor.db.JpaRepository;
import com.nbkr.db.CurrencyRate;

public class CurrencyRateRepository extends JpaRepository<CurrencyRate> {

  public CurrencyRateRepository() {
    super(CurrencyRate.class);
  }
}
