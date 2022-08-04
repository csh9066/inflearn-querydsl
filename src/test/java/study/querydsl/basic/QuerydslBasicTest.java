package study.querydsl.basic;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.JpaTestData;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@Rollback(value = false)
@Transactional
@SpringBootTest
public class QuerydslBasicTest {

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
    void startJPQL() {
        Member member = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "memberA")
                .getSingleResult();

        assertThat(member.getAge()).isEqualTo(10);
    }

    @Test
    void startQuerydsl() {
        Member result = jpaQueryFactory
                .select(member)
                .from(member)
                .where(member.age.between(10, 30)
                        .and(member.username.eq("memberA"))
                )
                .fetchOne();

        assertThat(result.getAge()).isEqualTo(10);
    }

    /**
     * 1. 회원 나이 내림차순 (desc)
     * 2. 회원 이름 올리차순 (asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력 (null last)
     */
    @Test
    void sort() {
        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member)
                .orderBy(
                        member.age.desc(),
                        member.username.asc().nullsLast()
                ).fetch();

        assertThat(result)
                .first()
                .extracting("age")
                .isEqualTo(100);

        assertThat(result)
                .last()
                .extracting("username")
                .isNull();
    }

    @Test
    void paging() {
        List<Member> result = jpaQueryFactory
                .select(member)
                .from(member)
                .offset(1)
                .limit(2)
                .fetch();

        Long count = jpaQueryFactory
                .select(member.count())
                .from(member)
                .fetchOne();

        assertThat(result).hasSize(2);
        assertThat(count).isEqualTo(6);
    }

    @Test
    void aggregation() {
        Tuple result = jpaQueryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetchOne();

        assertThat(result.get(member.count()))
                .isEqualTo(6);
        assertThat(result.get(member.age.sum()))
                .isEqualTo(180);
        assertThat(result.get(member.age.avg()))
                .isEqualTo(30);
        assertThat(result.get(member.age.max()))
                .isEqualTo(100);
        assertThat(result.get(member.age.min()))
                .isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구하라
     */
    @Test
    void group() {
        List<Tuple> result = jpaQueryFactory
                .select(
                        team.name,
                        member.age.avg()
                )
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamATuple = result.get(0);
        Tuple teamBTuple = result.get(1);

        assertThat(teamATuple.get(team.name))
                .isEqualTo("teamA");
        assertThat(teamATuple.get(member.age.avg()))
                .isEqualTo(15);

        assertThat(teamBTuple.get(team.name))
                .isEqualTo("teamB");
        assertThat(teamBTuple.get(member.age.avg()))
                .isEqualTo(20);
    }

    @Test
    void join() {
        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting("username")
                .containsExactly("memberA", "memberB");
    }


    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA 팀만 조인, 회원운 모두 조회 -> outer 조인 사용해야함
     * 참고로 on 말고 where를 사용할 경우 teamA가 아닌 회원들을 조회하지 않음
     */
    @Test
    void join_on_filtering() {
        List<Tuple> result = jpaQueryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    void fetchJoin() {
        em.flush();
        em.clear();

        Member result = jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("memberA"))
                .fetchOne();

        assertThat(result.getTeam().getName())
                .isEqualTo("teamA");
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    void subQuery_when_age_oldest() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).hasSize(1);
        assertThat(result).element(0)
                .extracting("age")
                .isEqualTo(100);
    }

    @Test
    void subQuery_when_age_avg_goe() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .orderBy(member.age.desc())
                .fetch();

        assertThat(result).hasSize(2);
        assertThat(result).element(0)
                .extracting("age")
                .isEqualTo(100);
    }
    
    @Test
    void subQueryIn() {
        QMember memberSub = new QMember("memberSub");

        List<Member> result = jpaQueryFactory
                .selectFrom(member)
                // 30 이상인 age를 조회해 in 조건에 주기
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.goe(30))
                ))
                .orderBy(member.age.desc())
                .fetch();

        assertThat(result).hasSize(2);
        assertThat(result).element(0)
                .extracting("age")
                .isEqualTo(100);
    }

    // column ex -> {username}, A
    @Test
    void constant() {
        List<Tuple> result = jpaQueryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        result.forEach(System.out::println);
    }

    // column ex -> {username}_{age}
    @Test
    void concat() {
        List<String> result = jpaQueryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.isNotNull())
                .fetch();

        result.forEach(System.out::println);
    }

}
