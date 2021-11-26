package com.example.api.controllers.payload;

import com.example.api.user.User;
import com.example.api.user.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Date;

@Getter @Setter @NoArgsConstructor
@ToString
public class UserList {

    private Long id;
    private String login;
    private String fullName;
    private UserRole userRole;
    private String url_avatar;
    private boolean active;
    private String createdBy;
    private Date creationDate;
    private String lastModifiedBy;
    private Date lastModifiedDate;

    public UserList(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.fullName = user.getFullName();
        this.userRole = user.getUserRole();
        this.url_avatar = user.getUrl_avatar();
        this.active = user.getActive();
        this.createdBy = user.getCreatedBy();
        this.creationDate = getCreationDate();
        this.lastModifiedBy = getLastModifiedBy();
        this.lastModifiedDate = getLastModifiedDate();
    }
}
