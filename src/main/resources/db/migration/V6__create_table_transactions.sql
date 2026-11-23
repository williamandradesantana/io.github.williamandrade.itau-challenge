create table tb_transactions (
  id uuid primary key,
  amount decimal(10, 2) not null,
  datetime timestamp with time zone not null,

  constraint chk_amount check (amount >= 0)
);