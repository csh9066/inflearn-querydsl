package study.querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Transactional
@SpringBootTest
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    void creation() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamA");
        em.persist(teamA);
        em.persist(teamB);


        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 20, teamA);

        Member memberC = new Member("memberC", 30, teamB);
        Member memberD = new Member("memberD", 40, teamB);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);

        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class)
                .getResultList();

        members.forEach(System.out::println);
    }
}
