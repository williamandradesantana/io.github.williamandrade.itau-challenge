create table tb_users (
  id uuid primary key not null,
  user_name varchar(30) not null,
  full_name varchar(150) not null,
  password varchar(255) not null,
  account_non_expired boolean not null default true,
  account_non_locked boolean not null default true,
  credentials_non_expired boolean not null default true,
  enabled boolean not null default true,

  constraint uk_user_name unique (user_name)
);