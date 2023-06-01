package org.ozyegin.cs.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository extends JdbcDaoSupport {

  private final String getProductById = "SELECT * FROM PRODUCT WHERE id = ?";
  private final String getProductsByBrandName = "SELECT * FROM PRODUCT WHERE brand_name = ?";
  private final String deleteProductById = "DELETE FROM PRODUCT p WHERE p.id = ?";
  private final String deleteAllProducts = "DELETE FROM PRODUCT";

  private final String createProduct = "INSERT INTO PRODUCT (name, description, brand_name) VALUES (?, ?, ?) RETURNING id";
  private final String updateProducts = "UPDATE PRODUCT SET name = ?, description = ?, brand_name = ? WHERE id = ?";


  private final RowMapper<Product> productRowMapper = (resultSet, rowNum) -> {
    Product newProduct = new Product();
    newProduct.setId(resultSet.getInt("id"));
    newProduct.setName(resultSet.getString("name"));
    newProduct.setDescription(resultSet.getString("description"));
    newProduct.setBrandName(resultSet.getString("brand_name"));
    return newProduct;
  };

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public Product find(int id) {
    try {
      return Objects.requireNonNull(getJdbcTemplate()).queryForObject(getProductById, productRowMapper, id);
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
  }

  public List<Product> findMultiple(List<Integer> ids) {
    List<Product> products = new ArrayList<>();

    ids.forEach(id -> {
      Product product = find(id);
      if (product != null) products.add(product);
    });

    return products;
  }

  public List<Product> findByBrandName(String brandName) {
    return Objects.requireNonNull(getJdbcTemplate()).query(getProductsByBrandName, productRowMapper, brandName);
  }

  public List<Integer> create(List<Product> products) {
    return products.stream()
            .map(product -> Objects.requireNonNull(getJdbcTemplate()).queryForObject(createProduct, Integer.class, product.getName(), product.getDescription(), product.getBrandName()))
            .collect(Collectors.toList());}

  public void update(List<Product> products) {
    products.forEach(product -> {
      Objects.requireNonNull(getJdbcTemplate()).update(updateProducts,
              product.getName(), product.getDescription(), product.getBrandName(), product.getId());
    });
  }

  public void delete(List<Integer> ids) {
    ids.forEach(id -> {
      Objects.requireNonNull(getJdbcTemplate()).update(deleteProductById, id);
    });
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllProducts);
  }
}
