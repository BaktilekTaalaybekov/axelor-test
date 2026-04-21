package com.nbkr.service;

import com.nbkr.db.CurrencyRate;
import com.nbkr.db.repo.CurrencyRateRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.db.JPA;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NbkrCurrencyRateService {

  private static final Logger LOG = LoggerFactory.getLogger(NbkrCurrencyRateService.class);
  private static final String NBKR_DAILY_URL = "https://www.nbkr.kg/XML/daily.xml";
  private static final DateTimeFormatter NBKR_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter ISO_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  @Inject private CurrencyRateRepository currencyRateRepository;

  @Transactional
  public void importDailyRates() {
    JPA.runInTransaction(this::doImportDailyRates);
  }

  @Transactional
  public void importDailyRates(ActionRequest request, ActionResponse response) {
    try {
      AtomicInteger importedCount = new AtomicInteger(0);
      JPA.runInTransaction(() -> importedCount.set(doImportDailyRates()));
      response.setFlash("NBKR import completed. Updated/created records: " + importedCount.get());
    } catch (Exception e) {
      response.setError("NBKR import failed: " + e.getMessage());
      throw e;
    }
  }

  private int doImportDailyRates() {
    try {
      LOG.info("NBKR import started");
      Document document = fetchDocument();
      Element root = document.getDocumentElement();
      LocalDate rateDate = parseRateDate(root.getAttribute("Date"));

      NodeList currencies = root.getElementsByTagName("Currency");
      int importedCount = 0;
      for (int i = 0; i < currencies.getLength(); i++) {
        Element currencyElement = (Element) currencies.item(i);
        try {
          String code = currencyElement.getAttribute("ISOCode");
          if (code == null || code.trim().isEmpty()) {
            LOG.warn("NBKR import: skipped currency with empty ISOCode");
            continue;
          }
          String name = readText(currencyElement, "Name");
          if (name == null || name.trim().isEmpty()) {
            name = code;
          }

          int nominal = parseNominal(readText(currencyElement, "Nominal"));
          BigDecimal rate = parseRate(readText(currencyElement, "Value"));

          CurrencyRate existing =
              currencyRateRepository
                  .all()
                  .filter("self.code = :code AND self.rateDate = :rateDate")
                  .bind("code", code)
                  .bind("rateDate", rateDate)
                  .fetchOne();

          CurrencyRate currencyRate = existing != null ? existing : new CurrencyRate();
          currencyRate.setCode(code);
          currencyRate.setName(name.trim());
          currencyRate.setNominal(nominal);
          currencyRate.setRate(rate);
          currencyRate.setRateDate(rateDate);

          currencyRateRepository.save(currencyRate);
          importedCount++;
        } catch (Exception itemError) {
          LOG.warn(
              "NBKR import: failed to process currency node #{}: {}",
              i,
              itemError.getMessage());
        }
      }
      LOG.info("NBKR import finished for date {}. Updated/created records: {}", rateDate, importedCount);
      return importedCount;
    } catch (Exception e) {
      LOG.error("NBKR import failed", e);
      throw new RuntimeException("Failed to import NBKR daily rates", e);
    }
  }

  private Document fetchDocument() throws Exception {
    HttpURLConnection connection = (HttpURLConnection) URI.create(NBKR_DAILY_URL).toURL().openConnection();
    connection.setRequestMethod("GET");
    connection.setConnectTimeout(10000);
    connection.setReadTimeout(10000);
    connection.setRequestProperty("Accept", "application/xml");

    int status = connection.getResponseCode();
    if (status < 200 || status >= 300) {
      throw new RuntimeException("NBKR endpoint returned HTTP status: " + status);
    }

    try (InputStream inputStream = connection.getInputStream()) {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(false);
      Document document = factory.newDocumentBuilder().parse(inputStream);
      document.getDocumentElement().normalize();
      return document;
    } finally {
      connection.disconnect();
    }
  }

  private LocalDate parseRateDate(String rawDate) {
    try {
      return LocalDate.parse(rawDate, NBKR_DATE_FORMAT);
    } catch (DateTimeParseException e) {
      return LocalDate.parse(rawDate, ISO_DATE_FORMAT);
    }
  }

  private int parseNominal(String rawNominal) {
    return Integer.parseInt(rawNominal.trim());
  }

  private BigDecimal parseRate(String rawRate) {
    return new BigDecimal(rawRate.trim().replace(",", "."));
  }

  private String readText(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return "";
    }
    return nodes.item(0).getTextContent();
  }
}
