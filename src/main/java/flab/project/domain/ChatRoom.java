package flab.project.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class ChatRoom extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 6434598345983495L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int participantCount;

    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;

    @OneToMany(mappedBy = "chatRoom")
    private List<ChatParticipant> chatParticipants = new ArrayList<>();

    private ChatRoom(String name, int participantCount, ChatRoomType chatRoomType) {
        this.name = name;
        this.participantCount = participantCount;
        this.chatRoomType = chatRoomType;
    }

    public void plusParticipantCount(int count) {
        participantCount += count;
    }

    public static ChatRoom createChatRoom(String name, int participantCount, ChatRoomType chatRoomType) {
        return new ChatRoom(name, participantCount, chatRoomType);
    }
}
