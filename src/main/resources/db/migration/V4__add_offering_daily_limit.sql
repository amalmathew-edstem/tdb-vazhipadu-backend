alter table offering add column daily_limit integer check (daily_limit > 0);

create index idx_order_item_offering_id on order_item (offering_id, order_id);
