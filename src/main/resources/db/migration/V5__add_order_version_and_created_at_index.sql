alter table orders add column version bigint default 0 not null;

create index idx_orders_created_at on orders (created_at);
