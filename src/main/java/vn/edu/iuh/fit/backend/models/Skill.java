package vn.edu.iuh.fit.backend.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.edu.iuh.fit.backend.enums.SkillType;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "skill")
@JsonIgnoreProperties({"jobSkills"})//để loại bỏ các thuộc tính không cần thiết từ quá trình serialize.
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id", nullable = false)
    private Long id;

    @Column(name = "skill_desc")
    private String skillDescription;

    @Column(name = "skill_name")
    private String skillName;

    @Column(name = "skill_type")
    private SkillType type;
    @OneToMany(mappedBy = "skill")
    private List<JobSkill> jobSkills;



    public Skill(String skillDescription, String skillName, SkillType type) {
        this.skillDescription = skillDescription;
        this.skillName = skillName;
        this.type = type;
    }

    public Skill() {

    }
}