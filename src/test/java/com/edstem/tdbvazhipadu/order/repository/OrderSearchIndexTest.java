package com.edstem.tdbvazhipadu.order.repository;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class OrderSearchIndexTest {

    private final JdbcTemplate jdbcTemplate;

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "phone_number = '9876543210' | IDX_ORDERS_PHONE_NUMBER_OFFERING_DATE",
                "status = 'BOOKED' | IDX_ORDERS_STATUS_OFFERING_DATE",
                "offering_date = DATE '2026-11-02' | IDX_ORDERS_OFFERING_DATE"
            })
    void commonSearchesUseAnIndexInsteadOfScanningTheTable(String condition, String expectedIndex) {
        String plan = jdbcTemplate.queryForObject(
                "explain select id from orders where " + condition
                        + " order by offering_date desc, id desc offset 0 rows fetch next 20 rows only",
                String.class);

        assertThat(plan).containsIgnoringCase(expectedIndex);
    }
}
