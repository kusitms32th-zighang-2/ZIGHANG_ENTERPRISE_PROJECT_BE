package zighang2.zighang.web.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import zighang2.zighang.global.common.BaseEntity;
import zighang2.zighang.web.domain.JobGroup;
import zighang2.zighang.web.domain.JobPosition;

@Entity
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter
@Table(name = "user_job_positions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"job_position_id", "user_id"})
        })
public class UserJobPosition extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_job_position_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "job_position_id")
    private JobPosition jobPosition;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
