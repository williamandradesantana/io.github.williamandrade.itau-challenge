insert into tb_users (
    id,
    user_name,
    full_name,
    password,
    account_non_expired,
    account_non_locked,
    credentials_non_expired,
    enabled
)
values
(
    gen_random_uuid(),
    'admin',
    'Administrator',
    '$2a$10$Z0K7sbAT9hJFlGbdV3yRKOBpDTxvTm9q42nPHqlbNYxqgJC43qDOG',
    true, true, true, true
),
(
    gen_random_uuid(),
    'user',
    'Common User',
    '$2a$10$Xa0Z5cjMpSRP1QJv.gXq.euJdl52vgVlQ0QdET7iN4EP6H9aRyhpW',
    true, true, true, true
)
on conflict (user_name) do nothing;



insert into tb_user_permissions (user_id, permission_id)
select u.id, p.id
from tb_users u
         join tb_permissions p on
    (u.user_name = 'admin' and p.name = 'ADMIN')
        or
    (u.user_name = 'user' and p.name = 'COMMON')
on conflict (user_id, permission_id) do nothing;