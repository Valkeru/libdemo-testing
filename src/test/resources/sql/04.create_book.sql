INSERT INTO library.book (id, "name", isbn, created_at, updated_at)
VALUES ('989b056f-31ef-4682-8f64-a21743939aab', 'test_d29827772a', '978-5-17-049678-5', NOW(), NOW())
ON CONFLICT DO NOTHING;

INSERT INTO library.book_author (author_id, book_id)
VALUES ('84c1599c-21e6-47f3-a03b-12f6071da20b', '989b056f-31ef-4682-8f64-a21743939aab')
ON CONFLICT DO NOTHING;
