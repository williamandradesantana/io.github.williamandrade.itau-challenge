alter table tb_permissions
    add constraint uk_permissions_name unique (name);

insert into tb_permissions (id, name)
values (gen_random_uuid(), 'COMMON'), (gen_random_uuid(), 'ADMIN')
on conflict (name) do nothing;
