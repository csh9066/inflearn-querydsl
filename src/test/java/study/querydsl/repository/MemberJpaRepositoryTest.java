package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class MemberJpaRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private MemberJpaRepository memberJpaRepository;


    @Test
    void searchWithBuilder(){
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("teamB");
        condition.setAgeGoe(31);
        condition.setAgeLoe(40);

        // when
        List<MemberTeamDto> result = memberJpaRepository.searchWithBuilder(condition);

        // then
        assertThat(result).hasSize(1);
        MemberTeamDto memberTeamDto = result.get(0);
        assertThat(memberTeamDto.getTeamName()).isEqualTo("teamB");
        assertThat(memberTeamDto.getAge()).isGreaterThanOrEqualTo(31);
        assertThat(memberTeamDto.getAge()).isLessThanOrEqualTo(40);
    }

    @Test
    void search() {
        // given
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
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

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setTeamName("teamB");
        condition.setAgeGoe(31);
        condition.setAgeLoe(40);

        // when
        List<MemberTeamDto> result = memberJpaRepository.search(condition);

        // then
        assertThat(result).hasSize(1);
        MemberTeamDto memberTeamDto = result.get(0);
        assertThat(memberTeamDto.getTeamName()).isEqualTo("teamB");
        assertThat(memberTeamDto.getAge()).isGreaterThanOrEqualTo(31);
        assertThat(memberTeamDto.getAge()).isLessThanOrEqualTo(40);
    }

}
