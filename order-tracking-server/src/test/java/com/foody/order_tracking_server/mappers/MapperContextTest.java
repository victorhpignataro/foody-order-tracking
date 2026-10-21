
package com.foody.order_tracking_server.mappers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MapperContextTest {

  @Autowired
  private OrderMapper orderMapper;

  @Autowired
  private OrderItemMapper orderItemMapper;

  @Test
  void mappersShouldBeRegisteredAsSpringBeans() {
    assertThat(orderMapper).isNotNull();
    assertThat(orderItemMapper).isNotNull();
  }
}
