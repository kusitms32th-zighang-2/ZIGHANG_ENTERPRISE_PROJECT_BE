package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
public class JobGroup extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_group_id")
    private Long id;

    @Column(name = "job_category", nullable = false)
    private String category;

    @OneToMany(fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();



}
