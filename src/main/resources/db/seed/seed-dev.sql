-- Dev seed data
-- Run: docker exec db psql -U app -d dspdemo -f /dev/stdin < src/main/resources/db/seed/seed-dev.sql

INSERT INTO advertisers (id, name)
VALUES ('45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b', 'Acme Corp')
ON CONFLICT (id) DO NOTHING;

INSERT INTO campaigns (id, name, landing_page, created_at, advertiser_id) VALUES
  (gen_random_uuid(), 'Summer Sale 2025',               'https://acme.com/summer-sale',          NOW() - INTERVAL '60 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Back to School',                 'https://acme.com/back-to-school',       NOW() - INTERVAL '55 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Black Friday Deals',             'https://acme.com/black-friday',         NOW() - INTERVAL '50 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Cyber Monday Blowout',           'https://acme.com/cyber-monday',         NOW() - INTERVAL '45 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Holiday Gift Guide',             'https://acme.com/holiday-gifts',        NOW() - INTERVAL '40 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'New Year New You',               'https://acme.com/new-year',             NOW() - INTERVAL '35 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Spring Collection Launch',       'https://acme.com/spring-collection',    NOW() - INTERVAL '30 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Valentine''s Day Special',       'https://acme.com/valentines',           NOW() - INTERVAL '25 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Flash Sale Weekend',             'https://acme.com/flash-sale',           NOW() - INTERVAL '20 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Loyalty Rewards Program',        'https://acme.com/loyalty',              NOW() - INTERVAL '18 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Product Launch Q2',              'https://acme.com/product-launch-q2',    NOW() - INTERVAL '15 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Referral Bonus Campaign',        'https://acme.com/referral',             NOW() - INTERVAL '12 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'App Download Push',              'https://acme.com/app',                  NOW() - INTERVAL '10 days', '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Brand Awareness Q1',             'https://acme.com/brand-q1',             NOW() - INTERVAL '8 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Retargeting - Cart Abandonment', 'https://acme.com/retarget-cart',        NOW() - INTERVAL '7 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Email Signup Incentive',         'https://acme.com/email-signup',         NOW() - INTERVAL '5 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Social Media Boost',             'https://acme.com/social',               NOW() - INTERVAL '4 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Influencer Collab Drop',         'https://acme.com/influencer-collab',    NOW() - INTERVAL '3 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Clearance Event',                'https://acme.com/clearance',            NOW() - INTERVAL '2 days',  '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b'),
  (gen_random_uuid(), 'Premium Membership Drive',       'https://acme.com/premium-membership',   NOW() - INTERVAL '1 day',   '45f3b3c9-97ef-4e3c-a455-c4f8b12e2f8b');
