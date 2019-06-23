INSERT INTO currency (id, name, abbr)
VALUES
  (1, 'US Dollar','USD'),
  (2, 'Euro', 'EUR'),
  (3, 'India Rupees', 'INR');

INSERT INTO transaction_status (id, name)
VALUES
       (1, 'Created'),
       (2, 'Processing'),
       (3, 'Failed'),
       (4, 'Succeed');

INSERT INTO bank_account (owner_name, balance, blocked_amount, currency_id)
VALUES
  ('Surender Singh', 1000.5, 0, 3),
  ('Samanyu Singh', 1000.5, 0, 2),
  ('Manisha', 1000.5, 0, 1);