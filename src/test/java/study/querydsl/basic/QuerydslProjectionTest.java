package study.querydsl.basic;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.JpaTestData;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

@Transactional
@SpringBootTest
public class QuerydslProjectionTest {

    @PersistenceContext
    private EntityManager em;

    private JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    void setUp() {
        JpaTestData jpaTestData = new JpaTestData(em);
        jpaTestData.create();

        jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Test
    void simpleProjectionPrint() {
        jpaQueryFactory
                .select(member.username)
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    @Test
    void tupleProjectionPrint() {
        List<Tuple> result = jpaQueryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);

            System.out.println("username = " + username);
            System.out.println("age = " + age);
        }
    }

    /**
     * 기본 생성자 필요, setter 필요
     */
    @Test
    void findDtoBySetter() {
        jpaQueryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    /**
     * 기본 생성자만 필요
     */
    @Test
    void findDtoByFields() {
        jpaQueryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    /**
     * fields 함수의 DTO 필드와 Q엔티티의 별칭이 다를 때 as 사용하기
     */
    @Test
    void findDtoByFieldsWhenNotEqualsFieldName() {
        jpaQueryFactory
                .select(Projections.fields(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    /**
     * 생성자를 이용한 프로젝션 생성자 인자에 따라 프로젝션 필드의 순서를 맞춰야 함
     */
    @Test
    void findDtoConstructor() {
        jpaQueryFactory
                .select(Projections.constructor(MemberDto.class,
                        // username와 age의 위치가 바뀌면 에러가남
                        member.username,
                        member.age
                        ))
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    @Test
    void findDtoByQueryProjection() {
        jpaQueryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch()
                .forEach(System.out::println);
    }

    @Test
    void dynamicQueryWithBooleanBuilder() {
        String usernameParam = "memberA";
        Integer ageParam = 10;

        List<Member> members = searchMember1(usernameParam, ageParam);

        assertThat(members).hasSize(1);
    }

    private List<Member> searchMember1(String username, Integer age) {
        BooleanBuilder builder = new BooleanBuilder();

        if (username != null) {
            builder.and(member.username.eq(username));
        }

        if (age != null) {
            builder.and(member.age.eq(age));
        }

        return jpaQueryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    void dynamicQueryWithWhereParam() {
        String usernameParam = "memberA";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);

        assertThat(result).hasSize(1);
    }

    private List<Member> searchMember2(String username, Integer age) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(condition(username, age))
                .fetch();
    }

    @Test
    void bulkUpdate() {
        jpaQueryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
    }

    private BooleanExpression usernameEq(String username) {
        return username == null ? null : member.username.eq(username);
    }

    private BooleanExpression ageEq(Integer age) {
        return age == null ? null : member.age.eq(age);
    }

    private Predicate condition(String username, Integer age) {
        return usernameEq(username).and(ageEq(age));
    }
}
