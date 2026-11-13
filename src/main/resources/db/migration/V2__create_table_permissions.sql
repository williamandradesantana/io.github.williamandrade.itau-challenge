create table tb_permissions (
  id uuid primary key not null,
  name varchar(20) not null,

  constraint chk_name check (name in ('COMMON', 'ADMIN'))
);