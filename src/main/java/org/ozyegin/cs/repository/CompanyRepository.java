package org.ozyegin.cs.repository;

import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;

import org.ozyegin.cs.entity.Company;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository extends JdbcDaoSupport {

  private final String getCompanyByName = "SELECT * FROM COMPANY WHERE name = ?";
  private final String getEmailsByCompanyName = "SELECT email FROM COMPANY_EMAIL WHERE company_name = ?";
  private final String getCompaniesByCountry = "SELECT * FROM COMPANY WHERE country = ?";
  private final String deleteCompany = "DELETE FROM COMPANY WHERE name = ?";
  private final String deleteAllCompanies = "DELETE FROM COMPANY";

  private final String createCompany = "INSERT INTO COMPANY (name, phone_number, country, city, zip_code, street_info) VALUES (?, ?, ?, ?, ?, ?)";
  private final String addEmailsForCompany = "INSERT INTO COMPANY_EMAIL VALUES (?, ?)";

  private final RowMapper<Company> companyRowMapper = (resultSet, rowNum) -> {
    Company newCompany = new Company();
    newCompany.setName(resultSet.getString("name"));
    newCompany.setCountry(resultSet.getString("country"));
    newCompany.setZip(resultSet.getInt("zip_code"));
    newCompany.setPhoneNumber(resultSet.getString("phone_number"));
    newCompany.setStreetInfo(resultSet.getString("street_info"));
    newCompany.setCity(resultSet.getString("city"));
    return newCompany;
  };

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public Company find(String name) {
    Company company = Objects.requireNonNull(getJdbcTemplate()).queryForObject(getCompanyByName, companyRowMapper, name);

    List<String> emails = Objects.requireNonNull(getJdbcTemplate()).queryForList(getEmailsByCompanyName, String.class, name);
    Objects.requireNonNull(company).setE_mails(emails);

    return company;
  }

  public List<Company> findByCountry(String country) {
    List<Company> companies = Objects.requireNonNull(getJdbcTemplate()).query(getCompaniesByCountry, companyRowMapper, country);

    companies.forEach(company -> {
      List<String> emails = Objects.requireNonNull(getJdbcTemplate()).queryForList(getEmailsByCompanyName, String.class, company.getName());
      Objects.requireNonNull(company).setE_mails(emails);
    });

    return companies;
  }

  public String create(Company company) throws Exception {
    Objects.requireNonNull(getJdbcTemplate()).update(createCompany, company.getName(), company.getPhoneNumber(), company.getCountry(), company.getCity(), company.getZip(), company.getStreetInfo());

    company.getE_mails().forEach(email -> {
      Objects.requireNonNull(getJdbcTemplate()).update(addEmailsForCompany, company.getName(), email);
    });

    return company.getName();
  }

  public String delete(String name) {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteCompany, name);
    return name;
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllCompanies);
  }
}
