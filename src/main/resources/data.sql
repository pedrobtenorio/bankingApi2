insert into accounts (id, balance, created_at, updated_at)
values
  ('11111111-1111-1111-1111-111111111111', 250.00, current_timestamp, current_timestamp),
  ('22222222-2222-2222-2222-222222222222', 500.00, current_timestamp, current_timestamp);

insert into ledger_entries (id, account_id, type, amount, occurred_at, description)
values
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 'CREDIT', 250.00, current_timestamp, 'initial credit'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '22222222-2222-2222-2222-222222222222', 'CREDIT', 500.00, current_timestamp, 'initial credit');
