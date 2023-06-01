package org.ozyegin.cs.repository;

import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionRepository extends JdbcDaoSupport {

  private final String deleteTransactionById = "DELETE FROM TRANSACTION WHERE id = ?";
  private final String deleteAllTransactions = "DELETE FROM TRANSACTION";

  private final String createTransaction = "INSERT INTO TRANSACTION (company_name, product_id, order_date, amount) VALUES (?, ?, ?, ?) RETURNING id";

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public Integer order(String company, int product, int amount, Date createdDate) {
    return Objects.requireNonNull(getJdbcTemplate()).queryForObject(createTransaction, Integer.class, company, product, new java.sql.Date(createdDate.getTime()), amount);
  }
  public void delete(int transactionId) throws Exception {
    int amount = Objects.requireNonNull(getJdbcTemplate()).update(deleteTransactionById, transactionId);
    if (amount != 1) {
      throw new IllegalArgumentException();
    }
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllTransactions);
  }
}
