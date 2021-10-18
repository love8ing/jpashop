package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStcokExceoption;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception{
        //given
        Member member = createMember();

        Book book = createBook("시골 JPA", 10000, 10);


        int orderCount =  2;
        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품 종류 수가 정확해야 한다", 1 ,getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("주문 수량만큼 재고가 줄어야 한다", 8 , book.getStockQuantity());
    }

   


    @Test
    public void 주문취소() throws Exception{
        //given
        Member member = createMember();
        Book Item = createBook("시골 JPA", 10000, 10);

        int orderCount= 2;

        Long orderId = orderService.order(member.getId(), Item.getId(), orderCount);
        //when

        orderService.cancelOrder(orderId);
        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문취소시 상태는 CANCEL 이다",OrderStatus.CANCEL , getOrder.getStatus());

        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.", 10, Item.getStockQuantity());
    }

    @Test(expected = NotEnoughStcokExceoption.class)
    public void 삼품주문_재고수량초과() throws Exception{

        //given
        Member member = createMember();
        Book Item = createBook("시골 JPA", 10000, 10);

        int orderCount= 11;
        //when
        orderService.order(member.getId(), Item.getId(), orderCount);

        //then
        fail("주문 수량 부족 예외가 발생해야 한다.");


    }


    private Book createBook(String name , int price, int stockQuantity) { //컨트롤 + 알트 + m
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "반포", "자이"));
        em.persist(member);
        return member;
    }
}