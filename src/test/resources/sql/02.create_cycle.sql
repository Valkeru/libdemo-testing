INSERT INTO library.cycle (id, "name", created_at, updated_at)
VALUES ('7cc6be9b-7649-4955-bff9-8cbf7c4c429a', 'test_9411799dad', now(), now()),
       ('7febe13e-19c2-4c12-82cc-b354546d360e', 'test_ba77861515', now(), now())
ON CONFLICT DO NOTHING;
