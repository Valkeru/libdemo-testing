INSERT INTO library.author (id, first_name, middle_name, last_name, created_at, updated_at)
VALUES ('84c1599c-21e6-47f3-a03b-12f6071da20b', 'test_9b844b884b', 'test_90321cca80', 'test_6012cf646d', NOW(), NOW()),
       ('2e0221f6-6fed-4407-aed8-a6ed90227f20', 'test_b562cf1fae', 'test_5f170fac03', 'test_a713816ac7', now(), now()),
       ('aa43b539-b5c1-48e6-829d-75326ea42a24', 'test_f7ba5093a9', 'test_006df0bfd6', 'test_01529219d0', now(), now())
ON CONFLICT DO NOTHING;
