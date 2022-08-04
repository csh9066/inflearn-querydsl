package study.querydsl.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@ToString(exclude = {"team"})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Member {

    @Id
    @GeneratedValue
    private Long id;

    private String username;

    private Integer age;

    @ManyToOne(fetch = FetchType.LAZY)
    private Team team;

    public Member(String username) {
        this(username, 0, null);
    }

    public Member(String username, Integer age) {
        this(username, age, null);
    }

    public Member(String username, Integer age, Team team) {
        this.username = username;
        this.age = age;
        if (team != null) {
            changeTeam(team);
        }
    }

    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
}
