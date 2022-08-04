package study.querydsl;

import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;


public class JpaTestData {
    private EntityManager em;

    public JpaTestData(EntityManager em) {
        this.em = em;
    }

    public void create() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);


        Member memberA = new Member("memberA", 10, teamA);
        Member memberB = new Member("memberB", 20, teamA);

        Member memberC = new Member("memberC", 30, teamB);
        Member memberD = new Member("memberD", 10, teamB);
        Member memberE = new Member(null, 10);
        Member memberF = new Member("memberF", 100);

        em.persist(memberA);
        em.persist(memberB);
        em.persist(memberC);
        em.persist(memberD);
        em.persist(memberE);
        em.persist(memberF);
    }
}
