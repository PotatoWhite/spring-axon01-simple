# Axon Framework을 이용한 Event Sourcing 구현 예제
- 본 예제는 https://github.com/eugenp/tutorials/tree/master/axon 의 예제를 기반으로 작성되었습니다.

## 순서

0. 사전 준비
1. Spring initializr를 이용한 프로젝트 생성 및 의존성 추가
2. Command Model 구현
3. Aggregate 구현
4. Query Model 구현 
5. Query Handler 구현
6. Endpoint 구현
7. Unit Test
8. 정리

---

# 0. 사전 준비

## axonserver on docker

```shell
docker run -d -e AXONIQ_AXONSERVER_NAME=order_demo -p 8024:8024 -p 8124:8124 -p 8224:8224 --name axonserver axoniq/axonserver
```

- https://docs.axoniq.io/reference-guide/v/4.0/operations-guide/setting-up-axon-server/launch
- Port 8024: Admin GUI (HTTP)
- Port 8124: gRPC (TCP - Axon Framework to Axon Server)
- Port 8224: gRPC (TCP - Axon Server to Axon Server: inter-nodes communication))

## Axon Server 사용법

- https://docs.axoniq.io/reference-guide/axon-server/administration
- http://localhost:8024

---

# 1. Spring initializr를 이용한 프로젝트 생성 및 의존성 추가

## Spring Initializr

- https://start.spring.io/
- dependencies
    - Spring Reactive Web
    - Lombok

## build.gradle axon framework 추가

```groovy
// axon
implementation 'org.axonframework:axon-spring-boot-starter:4.6.0'
implementation 'org.axonframework.extensions.reactor:axon-reactor-spring-boot-autoconfigure:4.6.0'
testImplementation 'org.axonframework:axon-test:4.6.0'
```

## application.yml

```yaml
axon:
  axonserver:
    servers: localhost:8124
```
---
# 2. Command Model 구현

## commands 패키지

- Axon Framework에서는 Command를 통해 Aggregate를 생성하고, 변경을 요청합니다.
- Command는 외부의 요청을 나타내는 객체입니다.
- Command Model의 목적은 어떤 Event가 발생할지를 결정하는 것입니다.

```java

@Data
@RequiredArgsConstructor
public class CreateOrderCommand {
  @TargetAggregateIdentifier
  private final String orderId;
  private final String productId;
}

@Data
@RequiredArgsConstructor
public final class ConfirmOrderCommand {
    @TargetAggregateIdentifier
    private final String orderId;
}

@Data
@RequiredArgsConstructor
public final class ShipOrderCommand {
  @TargetAggregateIdentifier
  private final String orderId;
  private final String address;
}
```

#### *TargetAggregateIdentifier

- 상태를 변경할 대상인 객제를 지정하기 위해 모든 Command에는 @TargetAggregateIdentifier가 있어야 합니다.
- @TargetAggregateIdentifier는 Command가 Aggregate에 전달될 때 Aggregate의 식별자를 결정하는데 사용됩니다.

## events 패키지

- Axon Framework에서는 Event를 통해 Aggregate의 상태를 변경합니다.
- Event는 Aggregate의 상태를 나타내는 객체입니다.

```java

@Data
@RequiredArgsConstructor
public final class OrderCreatedEvent {
    private final String orderId;
    private final String productId;
}

@Data
@RequiredArgsConstructor
public final class OrderConfirmedEvent {
    private final String orderId;
}

@Data
@RequiredArgsConstructor
public final class OrderShippedEvent {
    private final String orderId;
    private final String address;
}
```

---
# 3. Aggregate 구현
- 하나의 도메인객체를 나타내는 객체를 Aggregate라고 합니다. (앞으로는 객체를 Aggregate라고 부르겠습니다.)
- https://docs.axoniq.io/reference-guide/v/4.5/axon-framework/aggregate-framework
- CQRS 패턴에서 Aggregate Root 역할을 하고, Event Sourcing 패턴에서 Event Store 저장하는 역할을 합니다.
- Aggregate는 Axon Framework에서 가장 중요한 개념이며 Command와 Event를 발생시키는 역할을 합니다.

```java
@Aggregate
public class OrderAggregate {
  @AggregateIdentifier
  private String  orderId;
  private boolean orderConfirmed;

  protected OrderAggregate() {
  }

  @CommandHandler
  public OrderAggregate(CreateOrderCommand command) {
    AggregateLifecycle.apply(new OrderCreatedEvent(command.getOrderId(), command.getProductId()));
  }

  @EventSourcingHandler
  public void on(OrderCreatedEvent event) {
    this.orderId        = event.getOrderId();
    this.orderConfirmed = false;
  }

  @CommandHandler
  public void handle(ConfirmOrderCommand command) {
    if (orderConfirmed) return;

    AggregateLifecycle.apply(new OrderConfirmedEvent(command.getOrderId()));
  }

  @EventSourcingHandler
  public void on(OrderConfirmedEvent event) {
    this.orderConfirmed = true;
  }

  @CommandHandler
  public void handle(ShipOrderCommand command) {
    if (!orderConfirmed) throw new UnconfirmedOrderException(command.getOrderId());

    AggregateLifecycle.apply(new OrderShippedEvent(command.getOrderId(), command.getAddress()));
  }
}
```

#### *AggregateIdentifier
- Aggregate의 식별자를 나타내는 필드에 @AggregateIdentifier를 사용합니다.
- @AggregateIdentifier는 Aggregate의 식별자를 나타내는 필드에 사용되며, 이 필드는 CommandHandler에서 사용됩니다.

#### *ApplicationLifecycle - Axon Framework
- https://docs.axoniq.io/reference-guide/v/4.5/axon-framework/events/event-lifecycle
- ApplicationLifecycle은 Axon Framework에서 Command와 Event를 처리하는 라이프사이클을 관리합니다.
- apply() 메소드를 사용하면 Event를 생성하고(**event-store에 저장되고**), EventSourcingHandler를 통해 객체의 상태를 변경 할 수 있습니다.

---

# 4. Query 구현
- Query는 Aggregate의 상태를 조회하는데 사용됩니다.
- Aggregate의 상태를 조회하는데 사용되는 객체를 Query Model이라고 합니다.
- Query Model은 Aggregate의 상태를 조회하는데 사용되는 객체입니다.

## query 패키지
```java
public class FindAllOrderedProductsQuery {
}

@Data
@RequiredArgsConstructor
public class Order {
  private final String      orderId;
  private final String      productId;
  private       OrderStatus orderStatus = OrderStatus.CREATED;

  public void setOrderConfirmed() {
    this.orderStatus = OrderStatus.CONFIRMED;
  }

  public void setOrderShipped() {
    this.orderStatus = OrderStatus.SHIPPED;
  }
}

public enum OrderStatus {
  CREATED, CONFIRMED, SHIPPED
}
```

# 5. Query Handler 구현
- QueryHandler는 Query를 처리하는 메소드에 사용됩니다.
- QueryHandler의 목적은 발생된 event들을 통해 aggregate의 현재 상태를 조회하는 것입니다.
- QueryHandler vs Aggregate
  - QueryHandler는 Aggregate의 상태!
  - Aggregate는 Command와 Event를 처리!

```java
@Service
public class OrderEventHandler {

  // DB라고 가정하고 Map으로 구현
  private final Map<String, Order> orders = new HashMap<>();

  @EventHandler
  public void on(OrderCreatedEvent event) {
    orders.put(event.getOrderId(), new Order(event.getOrderId(), event.getProductId()));
  }

  @EventHandler
  public void on(OrderConfirmedEvent event) {
    orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
      order.setOrderConfirmed();
      return order;
    });
  }

  @EventHandler
  public void on(OrderShippedEvent event) {
    orders.computeIfPresent(event.getOrderId(), (orderId, order) -> {
      order.setOrderShipped();
      order.setAddress(event.getAddress());
      return order;
    });
  }

  @QueryHandler
  public List<Order> handle(FindAllOrderedProductsQuery query) {
    return orders.values().stream().toList();
  }
}
```
#### *참고 
- 모든 Event가 발생할 때마다 Query Model을 업데이트하고, 외부에서는 Query Model을 조회하는 방식으로 구현할 수 있습니다.

---
# 6. Endpoint 구현
- Endpoint는 Command와 Query를 처리하는 Component를 갖고 있습니다.
- Axon Framework는 CommandGateway와 QueryGateway를 제공합니다.

## RestController의 구현
```java
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("orders")
public class OrderRestEndpoint {
  private final ReactorCommandGateway commandGateway;
  private final ReactorQueryGateway   queryGateway;

  @PostMapping("ship-order")
  public Mono<Object> shipOrder(@RequestBody CreateOrderCommandRequest request) {
    log.info("Received request to ship order: {}", request);

    // create new OrderId and send command to ship order
    var orderId            = UUID.randomUUID().toString();
    var createOrderCommand = new CreateOrderCommand(orderId, request.productId());

    return commandGateway.send(createOrderCommand)
            .then(commandGateway.send(new ConfirmOrderCommand(orderId)))
            .then(commandGateway.send(new ShipOrderCommand(orderId, request.address())));
  }

  @GetMapping
  public Mono<List<Order>> getOrders() {
    return queryGateway.query(new FindAllOrderedProductsQuery(), ResponseTypes.multipleInstancesOf(Order.class));
  }
}
```
### @PostMapping("ship-order")
- CommandGateway를 CreateOrderCommand를 aggregate에 전달합니다. 이 후 Excetion이 발생하지 않으면
- ConfirmOrderCommand를 aggregate에 전달합니다. 이 후 Excetion이 발생하지 않으면
- ShipOrderCommand를 aggregate에 전달합니다.

### @GetMapping
- 조회 시는 Event Store에 있는 Event를 조회하는 것이 아니라, Query Model을 조회합니다.
- 이미 계산 된 Query Model을 조회하기 때문에 성능이 좋습니다.

---
# 7. unit test
- Axon Framework는 테스트를 위한 Test Fixture를 제공합니다.
- Test Fixture는 Command와 Query를 처리하는 Component를 갖고 있습니다.

## Test Fixture를 통한 Unit Test 구현
```java
class OrderAggregateTest {
  private FixtureConfiguration<OrderAggregate> fixture;

  private static final String ORDER_ID   = UUID.randomUUID().toString();
  private static final String PRODUCT_ID = UUID.randomUUID().toString();

  private static final String ADDRESS = "1234 Main Street, Anytown, USA";

  @BeforeEach
  public void setUp() {
    fixture = new AggregateTestFixture<>(OrderAggregate.class);
  }

  @Test
  public void testCreateOrder_success() {
    fixture.givenNoPriorActivity()
            .when(new CreateOrderCommand(ORDER_ID, PRODUCT_ID))
            .expectEvents(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID));
  }

  @Test
  public void ConfirmOrderCommand_success() {
    fixture.given(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID), new OrderConfirmedEvent(ORDER_ID))
            .when(new ShipOrderCommand(ORDER_ID, ADDRESS))
            .expectEvents(new OrderShippedEvent(ORDER_ID));
  }

  @Test
  public void ConfirmOrderCommand_fail() {
    fixture.given(new OrderCreatedEvent(ORDER_ID, PRODUCT_ID))
            .when(new ShipOrderCommand(ORDER_ID, ADDRESS))
            .expectException(UnconfirmedOrderException.class);
  }
}
```
### @BeforeEach
- Test Fixture를 생성합니다.

### @Test
- Test Fixture를 통해 Command를 전달하고, Event를 검증합니다.

---
# 8. 정리
## Axon Framework
- Axon Framework는 Command와 Query를 처리하는 Component를 제공합니다.
  - Command Model
    - Command는 Aggregate에 전달되고, Event를 발생시킵니다.
    - Aggregate는 Event를 Event Store에 저장합니다.
    - Event Store에 저장된 Event를 Event Processor인 EventHandler에 전달합니다.
  - Query Model
    - Query Model은 Event Store에 저장된 Event를 조회하여 계산합니다. 
    - 계산된 결과를 별도의 Query Model 저장소에 저장 할 수 있습니다.
    
## Axon Annotation
- @Aggregate
  - Aggregate를 정의합니다.
  - Aggregate는 Event를 발생시키는 Command를 처리합니다.
- @CommandHandler
  - Aggregate에 Command를 전달합니다.
  - Command를 처리하고, Event를 발생시킵니다.
- @EventSourcingHandler
  - Event를 처리합니다.
  - Event를 처리하여 Aggregate의 상태를 변경합니다.
- @EventHandler
- Event를 처리합니다.
  - Event를 처리하여 Query Model을 변경합니다.
- @QueryHandler
  - Query를 처리합니다.
### EventSourcingHandler와 EventHandler의 차이
- EventSourcingHandler는 Event를 처리하여 Aggregate의 상태를 변경합니다.
- EventHandler는 Event를 처리하여 Query Model을 변경합니다.
- EventSourcingHandler는 Aggregate에만 사용할 수 있습니다.
- EventHandler는 Query Model에만 사용할 수 있습니다.


---
# 참고
## *주의사항
- Java 17 의 record class는 Axon Framework에서 사용할 수 없습니다. (아직 - 다음버전 업데이트 예정)
- 덕분에 삽질 길게 했습니다. ㅠㅠ
- https://discuss.axoniq.io/t/are-java-records-supported/3908 
- 2022.02월 Posting 이지만 아직 4.6.x 버전에서는 지원하지 않습니다. ㅠㅠ

## Java 17의 record

- https://docs.oracle.com/en/java/javase/17/language/records.html
- https://docs.oracle.com/en/java/javase/17/language/records-specification.html
- https://docs.oracle.com/en/java/javase/17/language/records-constructor.html|
- Java 17의 record는 immutable이며 final입니다.
- 그 외츼 특성은
    1. Getter와 Setter를 자동으로 생성해줍니다.
    2. equals, hashCode, toString을 자동으로 생성해줍니다.
    3. 생성자를 자동으로 생성해줍니다.
    4. 상속을 허용하지 않습니다.
    5. final로 선언되어 있습니다.

# Command Model의 구현

- Command Model은 Command와 이것을 처리하는 Aggregate를 구현하는 것입니다.
- Command란, Command Model에서 발생하는 모든 것을 의미합니다.
- Aggregate란, Command Handler의 집합 입니다.

## Command의 구현

- Command는 java의 record로 구현합니다.
- Command는 Aggregate의 식별자를 가지고 있고 이것을 @TargetAggregateIdentifier로 지정합니다.

```java
public record CreateOrderCommand(@TargetAggregateIdentifier String orderId, String productId, int quantity) {
}

public record CancelOrderCommand(@TargetAggregateIdentifier String orderId) {
}

public record ShipOrderCommand(@TargetAggregateIdentifier String orderId) {
}
```