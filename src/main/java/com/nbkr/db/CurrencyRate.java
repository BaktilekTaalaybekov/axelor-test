package com.nbkr.db;

import com.axelor.db.Model;
import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "NBKR_CURRENCY_RATE")
public class CurrencyRate extends Model {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 3)
  private String code;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer nominal;

  @Column(nullable = false, precision = 18, scale = 6)
  private BigDecimal rate;

  @Column(name = "rate_date", nullable = false)
  private LocalDate rateDate;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Integer getNominal() {
    return nominal;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNominal(Integer nominal) {
    this.nominal = nominal;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public LocalDate getRateDate() {
    return rateDate;
  }

  public void setRateDate(LocalDate rateDate) {
    this.rateDate = rateDate;
  }

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public void setId(Long id) {
    this.id = id;
  }
}
