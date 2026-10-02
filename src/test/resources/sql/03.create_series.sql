INSERT INTO library.series (id, "name", cycle_id, created_at, updated_at)
VALUES ('697792d6-8d57-4d6f-9ea2-c91b01159612', 'test_42db2cab8e', '7cc6be9b-7649-4955-bff9-8cbf7c4c429a', now(), now()),
       ('99b17530-2c72-42ed-8055-73459af159c0', 'test_e2506e0e1a', '7febe13e-19c2-4c12-82cc-b354546d360e', now(), now())
ON CONFLICT DO NOTHING;
