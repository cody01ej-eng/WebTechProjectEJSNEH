create table rubric (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    name varchar(255) not null,
    primary key (id),
    constraint uk_rubric_name unique (name)
) engine=InnoDB;

create table rubric_criterion (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    rubric_id bigint not null,
    name varchar(255) not null,
    description varchar(1000) not null,
    max_score decimal(8,2) not null,
    display_order integer not null,
    primary key (id),
    constraint fk_rubric_criterion_rubric foreign key (rubric_id) references rubric (id)
) engine=InnoDB;

create table senior_design_section (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    name varchar(255) not null,
    start_date date not null,
    end_date date not null,
    rubric_id bigint not null,
    primary key (id),
    constraint uk_section_name unique (name),
    constraint fk_section_rubric foreign key (rubric_id) references rubric (id)
) engine=InnoDB;

create table senior_design_team (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    section_id bigint not null,
    name varchar(255) not null,
    description varchar(1000) not null,
    website_url varchar(255),
    primary key (id),
    constraint uk_team_name unique (name),
    constraint fk_team_section foreign key (section_id) references senior_design_section (id)
) engine=InnoDB;

create table user_account (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    first_name varchar(100) not null,
    middle_initial varchar(10),
    last_name varchar(100) not null,
    email varchar(255) not null,
    password_hash varchar(255) not null,
    role varchar(20) not null,
    status varchar(20) not null,
    section_id bigint,
    assigned_team_id bigint,
    primary key (id),
    constraint uk_user_email unique (email),
    constraint fk_user_section foreign key (section_id) references senior_design_section (id),
    constraint fk_user_team foreign key (assigned_team_id) references senior_design_team (id)
) engine=InnoDB;

create table invitation (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    token varchar(255) not null,
    email varchar(255) not null,
    type varchar(20) not null,
    status varchar(20) not null,
    section_id bigint,
    expires_at datetime(6) not null,
    message varchar(1000),
    primary key (id),
    constraint uk_invitation_token unique (token),
    constraint fk_invitation_section foreign key (section_id) references senior_design_section (id)
) engine=InnoDB;

create table active_week (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    section_id bigint not null,
    week_start_date date not null,
    active bit not null,
    primary key (id),
    constraint uk_active_week_section unique (section_id, week_start_date),
    constraint fk_active_week_section foreign key (section_id) references senior_design_section (id)
) engine=InnoDB;

create table team_instructor_assignment (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    team_id bigint not null,
    instructor_id bigint not null,
    primary key (id),
    constraint uk_team_instructor_assignment unique (team_id, instructor_id),
    constraint fk_team_instructor_assignment_team foreign key (team_id) references senior_design_team (id),
    constraint fk_team_instructor_assignment_user foreign key (instructor_id) references user_account (id)
) engine=InnoDB;

create table weekly_activity (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    student_id bigint not null,
    team_id bigint not null,
    active_week_id bigint not null,
    category varchar(30) not null,
    planned_activity varchar(255) not null,
    description varchar(2000) not null,
    planned_hours decimal(8,2) not null,
    actual_hours decimal(8,2) not null,
    status varchar(30) not null,
    primary key (id),
    constraint fk_weekly_activity_student foreign key (student_id) references user_account (id),
    constraint fk_weekly_activity_team foreign key (team_id) references senior_design_team (id),
    constraint fk_weekly_activity_week foreign key (active_week_id) references active_week (id)
) engine=InnoDB;

create table peer_evaluation_submission (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    author_id bigint not null,
    team_id bigint not null,
    active_week_id bigint not null,
    primary key (id),
    constraint uk_peer_evaluation_submission unique (author_id, active_week_id),
    constraint fk_peer_evaluation_submission_author foreign key (author_id) references user_account (id),
    constraint fk_peer_evaluation_submission_team foreign key (team_id) references senior_design_team (id),
    constraint fk_peer_evaluation_submission_week foreign key (active_week_id) references active_week (id)
) engine=InnoDB;

create table peer_evaluation_item (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    submission_id bigint not null,
    evaluatee_id bigint not null,
    public_comment varchar(1000),
    private_comment varchar(1000),
    primary key (id),
    constraint uk_peer_evaluation_item unique (submission_id, evaluatee_id),
    constraint fk_peer_evaluation_item_submission foreign key (submission_id) references peer_evaluation_submission (id),
    constraint fk_peer_evaluation_item_evaluatee foreign key (evaluatee_id) references user_account (id)
) engine=InnoDB;

create table peer_evaluation_criterion_score (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    item_id bigint not null,
    criterion_id bigint not null,
    score integer not null,
    primary key (id),
    constraint uk_peer_evaluation_criterion_score unique (item_id, criterion_id),
    constraint fk_peer_evaluation_score_item foreign key (item_id) references peer_evaluation_item (id),
    constraint fk_peer_evaluation_score_criterion foreign key (criterion_id) references rubric_criterion (id)
) engine=InnoDB;

create table requirement_reference (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    reference_key varchar(50) not null,
    title varchar(255) not null,
    source varchar(30) not null,
    summary varchar(1000) not null,
    primary key (id),
    constraint uk_requirement_reference_key unique (reference_key)
) engine=InnoDB;

create table traceability_link (
    id bigint not null auto_increment,
    created_at datetime(6) not null,
    updated_at datetime(6) not null,
    requirement_id bigint not null,
    component_type varchar(30) not null,
    component_name varchar(255) not null,
    status varchar(20) not null,
    notes varchar(1000) not null,
    primary key (id),
    constraint fk_traceability_link_requirement foreign key (requirement_id) references requirement_reference (id)
) engine=InnoDB;
