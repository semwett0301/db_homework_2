package org.ozyegin.cs.repository;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Objects;

@Repository
public class ProduceRepository extends JdbcDaoSupport {

  private final String deleteProduceById = "DELETE FROM PRODUCE WHERE id = ?";
  private final String deleteAllProduces = "DELETE FROM PRODUCE";
  private final String createProduce = "INSERT INTO PRODUCE (company_name, product_id, capacity) VALUES (?, ?, ?) RETURNING id";


  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public Integer produce(String company, int product, int capacity) {
    return Objects.requireNonNull(getJdbcTemplate()).queryForObject(createProduce, Integer.class, company, product, capacity);
  }

  public void delete(int produceId) throws Exception {
    int amount = Objects.requireNonNull(getJdbcTemplate()).update(deleteProduceById, produceId);

    if (amount != 1) {
      throw new IllegalArgumentException();
    }
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllProduces);
  }
}
