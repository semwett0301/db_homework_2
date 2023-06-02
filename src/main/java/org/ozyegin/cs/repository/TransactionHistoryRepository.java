package org.ozyegin.cs.repository;

import java.sql.Date;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionHistoryRepository extends JdbcDaoSupport {

  private final String getPairsWithMaxUsage = "SELECT avg_main.company_name as company_name, avg_main.product_id as product_id " +
          "FROM (SELECT tr_1.company_name, tr_1.product_id, COUNT(tr_1.amount) as avg_amount" +
          "  FROM TRANSACTIONS tr_1" +
          "  GROUP BY company_name, product_id) avg_main " +
          "JOIN (SELECT tmp_avg.company_name as company_name, max(tmp_avg.avg_amount_2) as max_amount" +
          "  FROM (SELECT tr_2.company_name as company_name, tr_2.product_id as product_id , COUNT(amount) as avg_amount_2" +
          "    FROM TRANSACTIONS tr_2" +
          "    GROUP BY company_name, product_id) tmp_avg" +
          "  GROUP BY tmp_avg.company_name) max_main " +
          "ON max_main.company_name = avg_main.company_name " +
          "AND max_main.max_amount = avg_main.avg_amount";

  private final String getUselessCompaniesForPeriod = "SELECT name as company_name " +
          "FROM COMPANY " +
          "EXCEPT " +
          "SELECT company_name " +
          "FROM TRANSACTIONS " +
          "WHERE order_date >= ? AND order_date <= ? ";
  private final RowMapper<Pair> pairMapper = (resultSet, i) -> new Pair(
      resultSet.getString("company_name"),
      resultSet.getInt("product_id")
  );

  private final RowMapper<String> stringMapper = (resultSet, i) -> resultSet.getString("company_name");

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public List<Pair> query1() {
    return Objects.requireNonNull(getJdbcTemplate()).query(getPairsWithMaxUsage, pairMapper);
  }

  public List<String> query2(Date start, Date end) {
    return Objects.requireNonNull(getJdbcTemplate()).query(getUselessCompaniesForPeriod, stringMapper, start, end);
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM TRANSACTIONS where is_active = FALSE");
  }
}
