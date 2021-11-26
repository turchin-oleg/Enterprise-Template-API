create table users
(
    id bigint not null,
    login varchar(125),
    password varchar(255),
    full_name varchar(255),
    email varchar(255),
    user_role varchar(20),
    lang varchar(10),
    theme varchar(10),
    active bit,
    token_exp bigint not null,
    url_avatar longtext,
    created_by varchar(255) not null,
    creation_date datetime(6) not null,
    last_modified_by varchar(255),
    last_modified_date datetime(6),
    primary key (id)
) engine=InnoDB;

alter table users add constraint UKow0gan20590jrb00upg3va2fn unique (login);

alter table users add constraint UK6dotkott2kjsp8vw4d0m25fb7 unique (email);

create table token_password_reset
(
    id bigint not null,
    expiration_date datetime(6),
    user_id bigint not null,
    primary key (id)
) engine=InnoDB;

alter table token_password_reset add constraint FKff5adf16vggh6fsrwhy45j6rd foreign key (user_id) references users (id);

create table users_sequence
(
    next_val bigint
) engine=InnoDB;

create table reset_pwd_token_sequence
(
    next_val bigint
) engine=InnoDB;
