alter table orders add column status varchar(20) default 'BOOKED' not null;

create index idx_orders_offering_date on orders (offering_date desc, id desc);

create index idx_orders_status_offering_date on orders (status, offering_date desc, id desc);

create index idx_orders_phone_number_offering_date on orders (phone_number, offering_date desc, id desc);
