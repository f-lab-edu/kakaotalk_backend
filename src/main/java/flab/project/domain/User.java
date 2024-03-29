package flab.project.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Getter
@NoArgsConstructor
@Audited
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String phoneNumber;

    private String nickname;

    private String profileImage;

    private String backgroundImage;

    private String statusMessage;

    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<UserAgreement> userAgreements = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    @NotAudited
    private List<Friend> friends = new ArrayList<>();

    @Builder
    public User(String email, String password, String name, String phoneNumber, String nickname, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.userRole = UserRole.USER;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public void updateBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public void updateStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
